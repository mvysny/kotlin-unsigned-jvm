# Float/Double accessors

## Why

The most obvious gap in the API, and the cheapest to close. Everything we compare against in
COMPARISON.md has it: Dart's `ByteData` (`getFloat32`/`getFloat64` — and we claim to mimic
`ByteData`), `ByteBuffer`, `MemorySegment`, korlibs. We don't. Anyone parsing a protocol frame with a
temperature or a voltage in it currently has to drop to `Float.fromBits(bytes.getInt(...))` by hand,
which is exactly the kind of thing this library exists to stop you doing.

Notably *not* a gap for Okio, which also has no float support — so we'd be leapfrogging it.

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
  `0x7FC00000`; `toRawBits()` preserves the exact bit pattern. For a serialisation library, a
  round-trip that silently rewrites bytes is wrong — some protocols use NaN payload bits as
  sentinels. Must be `toRawBits()` on the write side. (Read side is symmetric: `fromBits` is the
  only option and it preserves what it's given.) **This is the one non-obvious thing here** — worth
  a KDoc sentence and a dedicated test with a non-canonical NaN.
- **Naming: `getFloat`/`getDouble`, not `getFloat32`/`getFloat64`.** Dart uses the bit-width names,
  but our whole API is already named after the Kotlin types (`getInt`, not `getInt32`), and matching
  ourselves beats matching Dart. `ByteBuffer` agrees.
- **No widening overload.** `setShort(Int)` and `setUShort(UInt)` exist because truncating high bits
  is cheap, obvious and documented. A `setFloat(Double)` overload would silently *lose precision*,
  which is a different and much nastier failure. Don't add it.
- **Half-precision (`Float16`)?** korlibs has it; Dart's `ByteData` does not. Skip unless someone
  actually asks — Kotlin has no `Float16` type, so it'd have to return `Float`, which breaks the
  "named after the Kotlin type" rule above.

## Open questions

- Does this change the "mimics Dart's `ByteData`" framing in the README from aspiration to fact? If
  we add floats, the only remaining `ByteData` feature we lack is the typed-list views
  (`Float32List` etc.), which are a different concept entirely and arguably out of scope.
- `explicitApi()` means every one of these needs a KDoc block. The existing KDoc is copied nearly
  verbatim from Dart's docs (the "must be non-negative, and `byteOffset + 4` must be less than or
  equal to the length" phrasing) — keep that voice for consistency.
