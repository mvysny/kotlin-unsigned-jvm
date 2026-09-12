# Float/Double accessors

## Why

The most obvious gap in the API, and the cheapest to close. Everything we compare against in
design/comparison.md has it: Dart's `ByteData` (`getFloat32`/`getFloat64` — and we claim to mimic
`ByteData`), `ByteBuffer`, `MemorySegment`, korlibs. We don't. Anyone parsing a protocol frame with a
temperature or a voltage in it currently has to drop to `Float.fromBits(bytes.getInt(...))` by hand,
which is exactly the kind of thing this library exists to stop you doing.

Notably *not* a gap for Okio, which also has no float support — so we'd be leapfrogging it.

**But be honest about what kind of gap it is.** This is *positioning*, not demand. See
[Is this actually our gap?](#is-this-actually-our-gap) below. The multiplatform idea had exactly the
same character and was declined for it (`D_jvm_only` in design/decisions.md); this file should say so rather
than implying users are asking. The difference in this one's favour is that the cost is a few lines of
`fromBits`/`toRawBits` delegation, not a second core implementation and a rename.

## How binary formats actually serialize floats

Surveyed because it decides `toRawBits` vs `toBits`, and it turned out to be more uniform than
anything else in binary-protocol land: **the raw IEEE-754 bit pattern — binary32 for `Float`,
binary64 for `Double` — with byte order the only free variable.** No mainstream format does anything
else.

| Format | Encoding | Byte order |
|---|---|---|
| Dart `ByteData`, `ByteBuffer`, `DataOutputStream` | IEEE-754 raw bits | caller's / big |
| CBOR (RFC 8949) | IEEE-754 (half, single, double) | big |
| MessagePack | IEEE-754 | big |
| Thrift binary | IEEE-754, sent as the int64 bits | big |
| Protobuf `float`/`double` | IEEE-754 | little |
| Avro, BSON | IEEE-754 | little |
| WAV float PCM, Parquet, HDF5, numpy `.npy` | IEEE-754 | little |

Everyone is `memcpy`-ing the 4/8 bytes underneath, which is precisely what `toRawBits` is. So
`Float.fromBits(getInt(...))` isn't *a* reasonable design, it's *the* design.

The non-IEEE alternatives all live where this library will never go: IBM hex float (SEG-Y seismic,
GRIB1), VAX float, decimal strings (JSON and text protocols), and — the interesting one — scaled
integers, on which see below.

## Does `Endian` even apply to a float?

Yes, and for a reason worth writing down, because it's the first question anyone asks:

**Endianness applies to the container, not to the number.** IEEE-754 defines what a 32-bit *pattern*
means; it says nothing about which order those four bytes hit the wire. That is byte order — the
identical problem `getInt` already solves. Which is why delegating to `getInt`/`getLong` is not
merely convenient, it is exactly right: there is no format anywhere that reverses the mantissa bytes
but not the exponent bytes. Whole-word reversal is the entirety of it.

Two historical exceptions, both dead: ARM's pre-VFP FPA format stored doubles as two 32-bit words in
*swapped word order* on little-endian ARM (the notorious "ARM mixed-endian double"), and PDP-11 did
something similar. Gone since roughly 2005. Not worth a line of code.

The *live* version of that problem is Modbus register order, which is not float-specific at all —
`getUInt` has it today just as much. Split out into [[modbus-word-order]]; if that ever lands, floats
inherit it for free, since they're pure delegates to `getInt`/`getLong`.

## Shape

Follows the existing pattern exactly: primitives on `Endian`, one-line `inline` delegates in
`ByteArrays.kt`. Unlike the int widths, these need no new abstract members on `Endian` — they're pure
non-abstract wrappers over `getInt`/`getLong`, so `Endian.kt` grows by four functions and neither
enum constant changes at all:

```kotlin
// Endian.kt — non-abstract, alongside getUInt/setUInt
public inline fun getFloat(bytes: ByteArray, byteOffset: Int): Float =
    Float.fromBits(getInt(bytes, byteOffset))

public inline fun setFloat(bytes: ByteArray, byteOffset: Int, value: Float) {
    setInt(bytes, byteOffset, value.toRawBits())
}
// ...and getDouble/setDouble over getLong/setLong

// ByteArrays.kt
public inline fun ByteArray.getFloat(byteOffset: Int, endian: Endian = Endian.Big): Float =
    endian.getFloat(this, byteOffset)
```

That's ~8 functions plus KDoc. No new byte-shuffling logic, so the existing `EndianTest` coverage of
`getInt`/`getLong` already carries the correctness weight.

## Decisions to make

- **`toRawBits()`, not `toBits()`.** `toBits()` normalises every NaN to the canonical
  `0x7FC00000`; `toRawBits()` preserves the exact bit pattern. Every format in the table above
  specifies a bit pattern, so a library that silently rewrites one on write is non-conforming, not
  merely surprising — and some protocols use NaN payload bits as sentinels. Must be `toRawBits()` on
  the write side.

- **…but do not promise a NaN round trip.** The original draft of this file said "read side is
  symmetric: `fromBits` is the only option and it preserves what it's given". **That is false.**
  `Float.fromBits` compiles to `java.lang.Float.intBitsToFloat`, whose javadoc explicitly disclaims
  it: *"this method may not be able to return a float NaN with exactly the same bit pattern as the
  int argument"* — for some inputs `floatToRawIntBits(intBitsToFloat(x)) != x`. The lossy step is the
  read side *materialising a `Float`*, which we do not control.

  So the contract to document is: **this library never canonicalises; `setFloat` writes exactly the
  bits of the `Float` you hand it.** Not "NaN payloads survive a round trip through the byte array",
  which is a platform promise. Word it that way even though we are JVM-only (`D_jvm_only`): it costs
  nothing here and it is the wording that would survive a port. On JS a `Float` is a double at runtime
  and engines canonicalise NaN aggressively, so the stronger promise would be false there on day one.

- **Put them on `Endian`, not only in `ByteArrays.kt`.** There is no byte-shuffling to dispatch, so
  it's a fair question. Yes anyway: `Endian`'s non-abstract members are *already* pure
  type-conversion wrappers (`getUShort`, `setUInt`), `Endian` is public API that tests and users call
  directly, and floats would be the only asymmetry.

- **Naming: `getFloat`/`getDouble`, not `getFloat32`/`getFloat64`.** Dart uses the bit-width names,
  but our whole API is already named after the Kotlin types (`getInt`, not `getInt32`), and matching
  ourselves beats matching Dart. `ByteBuffer` agrees.

- **No widening overload — and it costs nothing to enforce.** Kotlin has no implicit numeric
  widening, so `bytes.setFloat(0, 1.0)` and `bytes.setDouble(0, 1.0f)` are *already* compile errors.
  The decision isn't "prevent a hazard", it's "don't manufacture one" by adding a `setFloat(Double)`
  overload that would silently lose precision. Worth a README line, because it's a genuine point of
  superiority over Dart: `ByteData.setFloat32` *has* to take a `double` and narrow silently, since
  Dart has no `float` type.

- **Half-precision (`Float16`)?** No — declined and graduated: `D_no_float16`. Nothing about the
  binary32/binary64 accessors depends on that answer.

## Testing

### The trap: the obvious NaN assertion silently passes

`kotlin.test.expect` resolves to the generic `assertEquals`, which boxes and calls
`java.lang.Float.equals` — which compares `floatToIntBits`, i.e. **canonicalises NaN**:

```kotlin
expect(Float.fromBits(0x7FC0DEAD)) { bytes.getFloat(1) }   // passes even if the payload was mangled
expect(0x7FC0DEAD) { bytes.getFloat(1).toRawBits() }       // actually tests it
```

Any payload assertion must go through `.toRawBits()`. Write this into a test comment, or someone
tidying later will "simplify" it back to the first form.

The mirror case is free: boxed equality *does* distinguish `0.0f` from `-0.0f` (the bits differ), so
the sign-bit test can be written the natural way.

Use a **quiet** NaN with a payload (`0x7FC0DEAD`), never a signalling one (`0x7F800001`) — sNaN is
exactly the case the javadoc carves out, and a 3-OS × 3-JDK matrix is the wrong place to gamble on it.

### The infrastructure already fits

`EndianTest.kt` already has `expect4`/`expect6`/`expect10`, writing at offset 1 into an oversized
array. `Float` → `expect6` (the `Int` helper), `Double` → `expect10` (the `Long` helper). No new
scaffolding, and the offset-1 convention already catches off-by-one shuffles.

### Vectors worth pinning (each in both endians)

`Float`: `1.0f`→`3F800000`, `-2.0f`→`C0000000`, `0.0f`→`00000000`, `-0.0f`→`80000000` (the sign-bit
test everyone forgets), `Float.MIN_VALUE`→`00000001` (subnormal), `+Inf`→`7F800000`, canonical
NaN→`7FC00000`, payload NaN→`7FC0DEAD` (raw-bits assertion only).

`Double`: `1.0`→`3FF0000000000000`, `-0.0`→`8000000000000000`, π→`400921FB54442D18`,
`Double.MIN_VALUE`→`0000000000000001`, `+Inf`→`7FF0000000000000`.

Note the KT-4749 wrinkle from AGENTS.md applies to the negative-signed double patterns: a literal
`0xFFF0000000000000` doesn't fit a `Long`. Use `Double.NEGATIVE_INFINITY` / the named constants, or
`"fff0000000000000".toULong(16).toLong()`.

## Is this actually our gap?

The Renogy observation behind this question has graduated on its own: **"how do I serialize a float"
and "how do I serialize a voltage" are different questions with different industry answers**, and
the second one is answered by scaled integers, not by IEEE-754. That finding, and the decision that
scaling stays out of this library entirely, are `D_no_unit_scaling`.

What it leaves for *this* idea is the honesty clause. design/comparison.md's "most obvious real gap"
is true as *feature-matrix* criticism — a reviewer comparing us to `ByteBuffer` will notice, and the
README currently has to concede the point mid-argument. It is not evidence that users are asking for
floats. The multiplatform idea had exactly the same character and was declined for it (`D_jvm_only`);
the difference in this one's favour is purely cost. The current doc wording slightly conflates
"cited gap" with "wanted feature"; fix that on graduation.

Still worth adding: eight functions, no new logic, closes a cell, completes the `ByteData` parity the
README already claims.

## Graduation is mostly a documentation edit

The float gap is load-bearing in the current docs' argument structure. Budget for six places, two of
them argumentative rather than factual, against ~8 functions of code:

| Place | What changes |
|---|---|
| `README.md` function list | four new bullets |
| `README.md` `ByteBuffer` section | "it throws in `Float`/`Double` … for free" stops being a concession |
| `design/comparison.md` summary table | our `A_floats` cell ❌ → ✅ |
| `design/comparison.md` "Where it loses" | bullet 1 deleted |
| `design/comparison.md` "Verdict" | recommendation 1 deleted, list renumbered |
| `design/decisions.md` | `Q_float_raw_bits` (bit patterns, and the round-trip non-promise) and `Q_floats_in_an_unsigned_library` — both become `D_` entries on graduation |

`Q_floats_in_an_unsigned_library` is the one that most needs writing: `Float` has no signed/unsigned
dimension, so "why does an unsigned library have floats?" is exactly the objection someone
re-proposes later. The answer is that the library is really *Dart's `ByteData` for Kotlin* and
`unsigned` in the name describes what was missing elsewhere, not the scope — a reading design/comparison.md
already committed to by making `A_floats` one of its axes. A rename would be the one moment the name
could stop fighting the scope, but `D_jvm_only` declined the multiplatform move that would have
forced one, so the name is staying as it is. Live with it, or argue the rename on its own merits.

KDoc voice: keep the near-verbatim Dart phrasing the rest of the API uses ("The `byteOffset` must be
non-negative, and `byteOffset + 4` must be less than or equal to the length of this object"), plus
one sentence on the raw-bits behaviour.

## Open questions

- Does this change the "mimics Dart's `ByteData`" framing in the README from aspiration to fact? If
  we add floats, the only remaining `ByteData` feature we lack is the typed-list views
  (`Float32List` etc.), which are a different concept entirely and arguably out of scope.
- Sequencing against multiplatform: moot, that one is declined (`D_jvm_only`). If it is ever revived,
  there is still no conflict — `fromBits`/`toRawBits` are common stdlib, so floats-first stays right.
- 24-bit accessors are the *other* gap design/comparison.md names. Not this file — filed separately as
  [[24bit-accessors]], where it turns out to be a much less free change than this one.
