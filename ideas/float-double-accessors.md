# Float/Double accessors

## Why

The most obvious gap in the API, and the cheapest to close. Everything we compare against in
COMPARISON.md has it: Dart's `ByteData` (`getFloat32`/`getFloat64` — and we claim to mimic
`ByteData`), `ByteBuffer`, `MemorySegment`, korlibs. We don't. Anyone parsing a protocol frame with a
temperature or a voltage in it currently has to drop to `Float.fromBits(bytes.getInt(...))` by hand,
which is exactly the kind of thing this library exists to stop you doing.

Notably *not* a gap for Okio, which also has no float support — so we'd be leapfrogging it.

**But be honest about what kind of gap it is.** This is *positioning*, not demand. See
[Is this actually our gap?](#is-this-actually-our-gap) below. The multiplatform idea had exactly the
same character and was declined for it (`D_jvm_only` in DECISIONS.md); this file should say so rather
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

- **Half-precision (`Float16`)?** No — see [Float16](#float16) below. If it ever comes up it's its
  own idea file, and `getHalf` is a better name than `getFloat16`.

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

Your Renogy observation is the real insight here and belongs in the record: **"how do I serialize a
float" and "how do I serialize a voltage" are different questions with different industry answers.**

IEEE-754 wins the first, overwhelmingly (table above). *Scaled integers* win the second, also
overwhelmingly — Renogy's ÷100, CAN/OBD-II's per-PID scale-and-offset, most of IEC 61850, most sensor
BLE. Embedded designers avoid floats because the MCU may have no FPU and because `2560` is exact
where `25.6f` is not. This library's origin — talking to a Renogy Rover — used no floats at all.

So COMPARISON.md's "most obvious real gap" is true as *feature-matrix* criticism, and a reviewer
comparing us to `ByteBuffer` will notice. It is not evidence that users want floats. The current doc
slightly conflates the two claims; fix that wording on graduation.

Still worth adding: eight functions, no new logic, closes a cell, completes the `ByteData` parity the
README already claims.

## Float16

"Float16" is **IEEE-754 binary16** (standardised 2008) — same anatomy as `Float`, smaller fields:

| | sign | exponent | mantissa | bias | max finite | smallest normal | ~decimal digits |
|---|---|---|---|---|---|---|---|
| binary16 (half) | 1 | 5 | 10 | 15 | 65504 | 6.10 × 10⁻⁵ | ~3.3 |
| binary32 (`Float`) | 1 | 8 | 23 | 127 | 3.40 × 10³⁸ | 1.18 × 10⁻³⁸ | ~7.2 |
| binary64 (`Double`) | 1 | 11 | 52 | 1023 | 1.80 × 10³⁰⁸ | 2.23 × 10⁻³⁰⁸ | ~15.9 |
| bfloat16 | 1 | 8 | 7 | 127 | 3.39 × 10³⁸ | 1.18 × 10⁻³⁸ | ~2.4 |

All the rules are identical — same exponent-bias scheme, same subnormals, same infinity and NaN
encodings — there's just less room. Practically: ~3 significant decimal digits, and it overflows to
infinity above 65504. Fine for a temperature or a normalised colour; useless for a distance in
millimetres.

`bfloat16` is the confusable sibling: binary32 with the low 16 mantissa bits chopped off, keeping the
*range* and discarding the *precision*. Conversion to/from `Float` is a shift, not an algorithm. It
exists for machine learning.

Where you meet binary16: CBOR (major type 7, additional info 25), OpenEXR, GPU texture/vertex data,
ML model files, occasionally sensor payloads. Where you meet bfloat16: ML only.

Three reasons to skip:

- **No Kotlin type.** It'd have to return `Float`, breaking the named-after-the-Kotlin-type rule.
- **No JDK help at our baseline.** JDK 20 added `Float.float16ToFloat(short)` /
  `floatToFloat16(float)`, but this library targets 17 — so ~20 hand-rolled lines whose subnormal and
  overflow edges would be the only real correctness risk in the whole float story.
- **The name is ambiguous in our own domain.** Bluetooth LE's health/battery profiles define
  `SFLOAT` — also 16 bits, but a 4-bit *decimal* exponent plus a 12-bit mantissa: scaled-integer
  thinking again, not IEEE. Anyone doing sensor work who sees `getFloat16` may expect that one.

## Graduation is mostly a documentation edit

The float gap is load-bearing in the current docs' argument structure. Budget for six places, two of
them argumentative rather than factual, against ~8 functions of code:

| Place | What changes |
|---|---|
| `README.md` function list | four new bullets |
| `README.md` `ByteBuffer` section | "it throws in `Float`/`Double` … for free" stops being a concession |
| `COMPARISON.md` summary table | our `A_floats` cell ❌ → ✅ |
| `COMPARISON.md` "Where it loses" | bullet 1 deleted |
| `COMPARISON.md` "Verdict" | recommendation 1 deleted, list renumbered |
| `DECISIONS.md` | `D_float_raw_bits` (bit patterns, and the round-trip non-promise) and `D_floats_in_an_unsigned_library` |

`D_floats_in_an_unsigned_library` is the one that most needs writing: `Float` has no signed/unsigned
dimension, so "why does an unsigned library have floats?" is exactly the objection someone
re-proposes later. The answer is that the library is really *Dart's `ByteData` for Kotlin* and
`unsigned` in the name describes what was missing elsewhere, not the scope — a reading COMPARISON.md
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
- 24-bit accessors are the *other* gap COMPARISON.md names. Deliberately not this file, and there's
  no idea file for it yet. Is it on the roadmap at all?
