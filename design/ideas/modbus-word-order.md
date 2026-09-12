# Modbus word order (middle-endian 32/64-bit values)

**Priority: low.** The library in its current two-constant shape drove a Renogy Rover to completion,
so `Endian.Big` was enough for the hardware this was built for. Everything below is
ecosystem-motivated — other people's Modbus devices — not need-driven. File it, don't schedule it.

## The problem

Modbus defines exactly one thing about byte order: a **16-bit register is big-endian on the wire**.
It says nothing whatsoever about 32-bit values, because it has none — a 32-bit quantity is two
registers by convention, and the spec never says which register holds the high half. Vendors picked
freely, so the field settled into four orderings. Every Modbus gateway, PLC tool and SCADA package on
earth has a four-item "word order" dropdown for this.

For the value `0x0A0B0C0D` (A=`0A`, B=`0B`, C=`0C`, D=`0D`):

| Wire bytes | Registers | What it is | Covered today? |
|---|---|---|---|
| `AB CD` | `[AB][CD]` | big-endian | ✅ `Endian.Big` |
| `CD AB` | `[CD][AB]` | big-endian registers, **low register first** | ❌ |
| `BA DC` | `[BA][DC]` | little-endian bytes, high register first | ❌ |
| `DC BA` | `[DC][BA]` | little-endian | ✅ `Endian.Little` |

We cover the two extremes and miss the two middles. **`CDAB` is the one that matters** — because
Modbus mandates big-endian *within* a register, vendors who deviate almost always deviate only on
which register carries the high half. `BADC` is comparatively rare (it comes from vendors who treat
the register pair as one little-endian 32-bit blob but keep register order).

## Why this library, of all places

This is the library's own origin story. It exists because of the Renogy Rover, which is Modbus RTU
over RS-485 — so this is the one protocol quirk the library plausibly ought to have an answer for.
Plausibly, not demonstrably: see the priority note above.

And note it is **not a float problem** — `getUInt` and `getULong` have it today, exactly as much as
[[float-double-accessors]] would. If this lands, floats inherit it for free, since they're pure
delegates to `getInt`/`getLong`.

It is also not a hack: "middle-endian" / "mixed-endian" is a real, named byte order (PDP-11 and
ARM's pre-VFP FPA doubles both did it). `Endian` is genuinely the right home for it — this is not a
protocol concern leaking into a byte-order type, it *is* a byte order.

## The model

Two independent axes, which is why there are exactly four:

- byte order **within** each 16-bit register
- order **of** the registers

So the rule for the new constants is clean: *take the base order, then reverse the sequence of 16-bit
registers.*

- `Big` = `AB CD` → registers reversed = `CD AB`
- `Little` = `DC BA` → registers reversed = `BA DC`

Which generalises correctly to 64-bit: `ABCDEFGH` has registers `[AB][CD][EF][GH]`, so word-swapped
big is `GH EF CD AB` — and note that is *not* the same as little-endian (`HG FE DC BA`). Good; that's
the whole point of the axis being independent.

At 16-bit width the word swap is a no-op, so the four constants collapse to two distinct behaviours
for `getShort`/`getUShort`. That's correct and unsurprising, but it needs saying in KDoc, or someone
will file it as a bug.

## Shape options

### 1. Two more enum constants (preferred)

`Endian { Big, BigWordSwapped, Little, LittleWordSwapped }`, each overriding the same six abstract
primitives with different index arithmetic. No new abstract members, no change to `ByteArrays.kt` at
all, no change to any call site. Matches the four-item dropdown users already know from their Modbus
tooling. Maybe 60 lines of very boring, very testable code.

Costs:

- **Source-compatibility hazard.** An exhaustive `when (endian)` in downstream code stops compiling,
  and a `when` *expression* compiled against the old two constants throws
  `NoWhenBranchMatchedException` at runtime if it ever sees a new one. Realistically nobody writes
  `when (endian)` — the library itself dispatches virtually and never does — but it's a real note for
  the release.
- Every KDoc in the library currently reads as if there were two orders. Some will need a second
  look.

### 2. `Endian` becomes a sealed class with a `WordSwapped(base)` wrapper

More composable and models the two axes explicitly. But it gives up `values()`, `valueOf()`,
`when`-exhaustiveness and name-based serialization, for a type that has exactly four inhabitants and
always will. Not worth it.

### 3. Per-call `wordSwapped: Boolean` parameter

Pollutes every signature in the public API with a flag that is meaningless for `getByte` and
`getShort`. No.

### 4. Do nothing; document the workaround

The honest baseline: users combine two `getUShort` calls themselves. Write it in the README as a
recipe. Cheap, and defensible if Modbus users aren't actually a constituency.

## Naming

The letter patterns (`ABCD`/`CDAB`/`BADC`/`DCBA`) are how Modbus tooling names these, and they're
unambiguous — but they're meaningless outside Modbus and they read badly for 16-bit and 64-bit
widths, where there aren't four letters.

`BigWordSwapped` / `LittleWordSwapped` derives from the existing names and generalises to any width.
Go with those, **and put the letter pattern in the KDoc**, because tool vendors disagree with each
other on the English names: the same `BADC` is "Big Endian Byte Swap" in one product and something
else in the next. The letters are the only reliable lingua franca.

## Testing

Same `expect4`/`expect6`/`expect10` helpers and the same hex-string style as `EndianTest`. The one
case worth being deliberate about is 64-bit: pin `GH EF CD AB`-style vectors explicitly, because
"word-swapped" at 64-bit is the case a reasonable person could implement three different ways
(reverse registers / reverse 32-bit halves / reverse everything).

## Open questions

- ~~**Does the Rover actually need it?**~~ **Answered: no.** The Rover driver was written against
  this library as it stands and worked fully, so whatever Renogy does with its 32-bit fields
  (cumulative generation, total amp-hours), `Endian.Big` handled it. Motivation here is the
  ecosystem, not the hardware — the same positioning-vs-demand distinction as
  [[float-double-accessors]] and the multiplatform idea (declined on exactly that ground; see
  `D_jvm_only` in design/decisions.md), except that here there is one fewer argument for acting, since the
  *other* two at least close cells competitors have filled.
- Does a general-purpose byte-array library want to carry a quirk whose only real constituency is one
  industrial protocol? Counter-argument in *Why this library* above; the decision should be recorded
  as `Q_word_order` (a `D_` entry in design/decisions.md on graduation) either way, because "why does `Endian` have four values?" is a
  question someone will ask in three years.
- Is this a better use of effort than [[float-double-accessors]]? It's ~60 lines against ~8 functions,
  it serves the domain the library was actually built for, and no competitor in design/comparison.md has it
  either — not `ByteBuffer`, not korlibs, not kotlinx-io. Netty doesn't have it. That's an *empty*
  cell in the comparison table rather than a cell where we're behind, which is a different and
  arguably better kind of win.
- Graduation would add an axis to the design/comparison.md table (`A_word_order`?) where every single
  competing row is ❌. Tempting — but only add the axis if it's a real axis and not a
  self-congratulatory one; the test is whether a reader choosing a library would weigh it.
- Unrelated but adjacent: **24-bit accessors**, the other gap design/comparison.md names (Netty and
  korlibs both have it). Now filed separately as [[24bit-accessors]] — and note it interacts with
  this idea, since three bytes is not a whole number of 16-bit registers.
