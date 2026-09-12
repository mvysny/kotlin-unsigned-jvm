# 24-bit accessors

**Priority: undecided** — and unlike the float accessors (shipped: `D_floats_in_scope`), this one is
not obviously cheap. Split out of the modbus file, which had it as a trailing "unrelated but
adjacent" note.

## The gap

design/comparison.md names it as one of this library's two gaps: **Netty** (`getMedium` /
`getUnsignedMedium` / `setMedium`, both endians) and **korlibs** both have 24-bit access; we don't,
and neither does `ByteBuffer`, `MemorySegment`, `DataInputStream`, kotlinx-io or Okio. So this is a
half-empty cell rather than a cell where we are behind everybody — a different, arguably better kind
of win than the float gap.

Where 24-bit fields actually turn up: 24-bit PCM audio (the reason Netty and the audio world care),
RGB pixels, 24-bit ADC readings from sigma-delta sensors, `mediumint` in MySQL's wire protocol,
plenty of ad-hoc industrial registers. It is a real width, not a curiosity.

## Why this is not a free change

Every other accessor in this library is a delegation — `getUInt` to `getInt`, and `getInt` to a
`VarHandle` (`D_varhandle`). **There is no byte-array-view `VarHandle` for 24 bits**, because there
is no 3-byte primitive to view the array as. So this is the first accessor that would have to do its
own byte shuffling, and it brings back exactly the shift-or arithmetic `D_varhandle` deleted:

```kotlin
// big-endian, unsigned, sketch only
(bytes[o].toUByte().toUInt() shl 16) or (bytes[o + 1].toUByte().toUInt() shl 8) or bytes[o + 2].toUByte().toUInt()
```

Consequences to weigh:

- **Bounds checking becomes ours.** Today the `VarHandle` throws, and `Endian`'s KDoc documents the
  odd message it produces ("index 2 out of bounds for length 1"). Three separate `bytes[o + n]`
  reads throw on whichever index goes out first, with a *byte*-counted message — so 24-bit would be
  the one width whose exception message doesn't match the documented quirk. Either accept a
  documented inconsistency or add an explicit range check, which costs a branch.
- **Signed means manual sign extension.** `getInt24` has to sign-extend bit 23 by hand
  (`(raw shl 8) shr 8`), and getting it wrong is silent: it only shows on negative values, which the
  obvious test vectors don't have. This is the single likeliest bug in the whole feature.
- **Performance is unmeasured.** Probably fine — C2 merges the three loads — but `D_varhandle` set a
  precedent of *measuring* rather than assuming, and this is the one accessor that can't inherit the
  measurement.

Three bytes is not much code, but it is the only code in the library that isn't obviously correct by
inspection.

## Naming, which is the real design question

Kotlin has no 24-bit type, so the accessor must return `Int` / `UInt` — and that breaks the rule the
whole API follows, that the name is the Kotlin type it hands back (`getInt`, not `getInt32`). This is
the *same* objection that declined half-precision (`D_no_float16`), and it should not be answered
differently here without saying why.

Candidates:

- `getInt24` / `getUInt24` — says the wire width, reads as a lie about the return type.
- `getMedium` / `getUnsignedMedium` — Netty's name, and prior art in exactly our niche. But
  "medium" means nothing outside Netty and MySQL, and `getUnsignedMedium` abandons the `U` prefix
  the rest of the API uses.
- `getUMedium` — consistent with `getUShort`/`getUInt`, but now it's a Netty name we've edited.

No candidate is clean. That is a reason to think harder, not a reason to decline — but it does mean
the decision is "what do we call it", not "should we".

## Open questions

- `Q_24bit_worth_it` — is protocol work actually the target use case? The library was built for one
  Modbus device (16-bit registers only). Same positioning-vs-demand test that declined multiplatform
  (`D_jvm_only`) and that the float accessors had to answer honestly before shipping on cost alone
  (`D_floats_in_scope`). The difference is that this one costs real code, so the answer matters more.
- `Q_24bit_naming` — the section above. Whatever wins must also explain why `D_no_float16`'s
  no-Kotlin-type objection doesn't apply, or concede that it does and that 24-bit is worth the
  exception anyway.
- Does it need `Endian` support on both constants? Yes, obviously — but note it also interacts with
  [[modbus-word-order]] in a way the other widths don't: three bytes is not an even number of 16-bit
  registers, so "word-swapped 24-bit" has no meaning. If both land, that needs one line of KDoc.
- Would `getUInt24` in `Endian` be abstract (per-constant override, like the other widths) or a
  single non-abstract implementation branching on `this`? The former matches the file; the latter
  avoids writing the shift arithmetic twice, which is where the bug lives.
