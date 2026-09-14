# Research — reading fixed-width integers out of a byte buffer on the JVM

What the things we don't own actually do. About *them*, never us: a sentence starting "we chose"
is a `D_`. `## R_<slug> — <title>`, one claim per bullet, one provenance marker per claim —
**[docs]**, **[src]**, **[verified <date>, <version>]**, **[unverified]** (a hypothesis; a design
built on it says so). Surveyed against the versions named in each entry, as of 2026-09-12; a
version-sensitive claim names the version it was seen on. Cite by slug, `R_<slug>`, never by
position; `grep '^## R_' design/research.md` is the index. The first entry is the ruler: every later
one trims to its length. Whether any of this means the library should exist is `D_library_exists`,
not this file.

---

## R_survey_matrix — the alternatives, on the axes that separate them

The axes: **offset access** — a multi-byte value read or written at an arbitrary byte offset, in any
order, the opposite being a consuming cursor (`readInt()` advances a pointer); **Kotlin unsigned** —
hands back `UByte`/`UShort`/`UInt`/`ULong` rather than widening into a signed `Int`/`Long`;
**endian per call** — byte order chosen at the call site rather than held as buffer state
(`ByteBuffer.order()`), baked into the name (`readIntLe`), or fixed by the library; **no wrapper** —
takes a plain `ByteArray`; **zero dep**; **multiplatform**; **floats** — IEEE-754 `Float`/`Double`.

The first row is the reference point, not a finding. **[verified 2026-09-12]** for the JDK and
Kotlin-stdlib rows, **[docs]** for the rest.

| | offset access | Kotlin unsigned | endian per call | no wrapper | zero dep | multiplatform | floats |
|---|---|---|---|---|---|---|---|
| **kotlin-unsigned-jvm** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ JVM only | ✅ |
| JDK `ByteBuffer` | ✅ | ❌ widens | ⚠️ buffer state | ❌ wrapper | ✅ | — | ✅ |
| JDK `VarHandle` byte-array view | ✅ | ❌ widens | ❌ baked into handle | ✅ | ✅ | — | ✅ |
| JDK `MemorySegment` (22+) | ✅ | ❌ widens | ✅ per-layout | ❌ wrapper | ✅ | — | ✅ |
| JDK `Data*Stream` | ❌ stream | ❌ widens | ❌ big-endian only | ❌ wrapper | ✅ | — | ✅ |
| Kotlin stdlib `getIntAt` | ✅ | ✅ | ❌ **undocumented** | ✅ | ✅ | ❌ Native only | ✅ |
| kotlinx-io | ❌ cursor | ✅ | ❌ in the name | ❌ wrapper | ⚠️ | ✅ | ✅ |
| Okio | ❌ cursor | ❌ signed only | ❌ in the name | ❌ wrapper | ⚠️ | ✅ | ❌ |
| Netty `ByteBuf` | ✅ | ❌ widens | ❌ in the name | ❌ wrapper | ❌ | ❌ | ✅ |
| Guava primitives | ⚠️ no offset | ❌ widens | ❌ big-endian only | ✅ | ❌ | ❌ | ❌ |
| Commons Lang3 `Conversion` | ✅ | ❌ widens | ❌ little-endian only | ✅ | ❌ | ❌ | ❌ |
| Commons IO `EndianUtils` | ✅ | ❌ widens | ❌ little-endian only | ✅ | ❌ | ❌ | ❌ |
| Apache POI `LittleEndian` | ✅ | ❌ widens | ❌ little-endian only | ✅ | ❌ | ❌ | ❌ |
| Bouncy Castle `Pack` | ✅ | ❌ signed only | ✅ in the name | ✅ | ❌ | ❌ | ❌ |
| korlibs `korlibs-memory` | ✅ | ❌ widens | ❌ in the name | ✅ | ❌ | ✅ | ✅ |

⚠️ = partial; the entry below says how.

## R_bytebuffer — `java.nio.ByteBuffer`: absolute accessors

- `ByteBuffer` has had **absolute, index-based** accessors since Java 1.4 — `getInt(index)`,
  `putInt(index, v)` — which do not touch the position. "A buffer with a pointer" is only true of the
  relative methods. **[docs]**
- `ByteBuffer.wrap(bytes).order(LITTLE_ENDIAN).getInt(4).toUInt()` is bit-for-bit identical to this
  library's `getUInt(4, Endian.Little)`, and `getLong(8).toULong()` to `getULong`.
  **[verified 2026-09-12, JDK 25]**
- Unsigned 16-bit has a JDK trick: `getChar(index)` reads an unsigned 16-bit value, `Char` being the
  JVM's unsigned 16-bit type. There is no equivalent for 32- or 64-bit. **[verified 2026-09-12, JDK 25]**
- Byte order is **state on the buffer**, not an argument: a frame with a big-endian header and a
  little-endian payload needs either two wrapper buffers or `order()` calls interleaved with the
  reads. **[docs]**
- `wrap()` allocates a wrapper — usually escape-analysed away or hoisted out of a loop, but an object
  to pass around alongside, or instead of, the array. **[docs]**
- It does everything a byte-poking library does not: bulk `get(byte[])`, `slice()`, direct/off-heap
  buffers, `CharBuffer`/`IntBuffer` views. **[docs]**

## R_varhandle_byte_array_view — `MethodHandles.byteArrayViewVarHandle`

- [`MethodHandles.byteArrayViewVarHandle`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/invoke/MethodHandles.html)
  views a `byte[]` as an `int[]`/`long[]`/`short[]` of the given byte order; the JIT compiles the
  access to a single, possibly byte-swapping, unaligned load or store. This is what Lucene and friends
  use, and the performance ceiling for this kind of code. **[docs]**
- Callable from Kotlin, and plain `get`/`set` accept misaligned offsets — offset 5 into an int view is
  fine. Only the atomic access modes require alignment. **[verified 2026-09-12, JDK 25]**
- The handle is untyped: every call site casts (`get(bytes, 4) as Int`) with no compile-time check
  that the cast matches the handle. **[verified 2026-09-12]**
- Byte order is baked into the handle, so covering short/int/long × big/little takes six static
  handles. **[docs]**
- Kotlin's support for signature-polymorphic calls has had compiler bugs as recently as
  [KT-72880](https://youtrack.jetbrains.com/issue/KT-72880), fixed in Kotlin 2.4.0. **[docs]**
- Out-of-range offsets throw `ArrayIndexOutOfBoundsException`, the array subclass, not a bare
  `IndexOutOfBoundsException`; the message counts in units of the *value's* width, so an `Int` read at
  offset 2 of a 4-byte array says "index 2 out of bounds for length 1".
  **[verified 2026-09-12, JDK 25]**

## R_memorysegment — `MemorySegment` (JDK 22+)

- The JDK's modern successor to `ByteBuffer`:
  `MemorySegment.ofArray(bytes).get(JAVA_INT_UNALIGNED.withOrder(LITTLE_ENDIAN), 4L)`. **[docs]**
- Byte order travels with the *layout* rather than with the buffer, so a mixed-endian frame needs two
  layout constants rather than interleaved state changes. **[docs]**
- Still signed-only, still a wrapper object, and it needs JDK 22. **[docs]**

## R_kotlin_native_byte_accessors — the Kotlin stdlib's `ByteArray.getIntAt` / `setUIntAt`

- They exist, return true unsigned types, and take a byte offset — but only in
  [`kotlin.native`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.native/get-short-at.html),
  only under `@ExperimentalNativeApi`. There is no common or JVM equivalent and no announced plan for
  one. **[docs, Kotlin 2.4]**
- **The byte order is undocumented** — the API reference states neither big, little nor
  platform-dependent. Re-checked against the 2.4 docs; unchanged. **[docs, Kotlin 2.4]**
- The likely reason is that these are raw memory reinterprets, which makes the order the host CPU's;
  nothing in the docs says so. **[unverified]**

## R_kotlinx_io — kotlinx-io 0.9.1

- The only surveyed library that returns **real Kotlin unsigned types**
  ([core API](https://kotlinlang.org/api/kotlinx-io/kotlinx-io-core/kotlinx.io/)): `readUByte`,
  `readUShort`/`readUShortLe`, `readUInt`/`readUIntLe`, `readULong`/`readULongLe`, with matching
  `writeU*`. Multiplatform. **[docs]**
- It is a `Source`/`Sink` **cursor** API: reads consume, and there is no random access to a multi-byte
  value at an arbitrary offset. **[docs]**
- Byte order lives in the function name, so choosing it from a just-parsed header means a `when`.
  **[docs]**
- Pre-1.0, so the API can still move. **[docs]**
- A different shape of problem: streaming and segmented buffers. **[docs]**

## R_okio — Okio 3.18.2

- Mature, very widely used, multiplatform, Kotlin-first — and **entirely signed**:
  `readInt`/`readIntLe`, `readShort`/`readShortLe`, `readLong`/`readLongLe`, no unsigned variants, in
  [`Buffer.kt`](https://github.com/square/okio/blob/master/okio/src/commonMain/kotlin/okio/Buffer.kt).
  **[src]**
- Cursor-based like kotlinx-io; `Buffer` offers indexed access for single bytes only. **[docs]**
- No `Float`/`Double` accessors. **[docs]**

## R_netty_bytebuf — Netty `ByteBuf` 4.2

- The most complete *random-access* unsigned API on the JVM
  ([`ByteBuf`](https://netty.io/4.2/api/io/netty/buffer/ByteBuf.html)): absolute
  `getUnsignedByte`/`getUnsignedShort`/`getUnsignedMedium`/`getUnsignedInt`, each with an `LE` twin,
  none of which move `readerIndex`. **[docs]**
- It has 24-bit ("medium") accessors, which most alternatives lack. **[docs]**
- Results are **widened** (`short`, `int`, `long`), and there is **no unsigned 64-bit** at all, Java
  having no type for it. **[docs]**
- Byte order is in the method name, and the buffer brings reference counting, pooling and a
  non-trivial dependency. **[docs]**

## R_jvm_utility_libs — Guava, Commons Lang3, Commons IO, POI, Bouncy Castle

- **Guava**: `Ints.fromByteArray` / `Longs.fromByteArray` are big-endian only and read from offset 0 —
  no offset parameter, so a field at offset 4 means `Ints.fromBytes(b[4], b[5], b[6], b[7])` or a copy.
  `UnsignedInts`/`UnsignedLongs`/`UnsignedBytes` help with *arithmetic* on widened values, which Kotlin
  gets for free from its unsigned types. `LittleEndianDataInputStream` covers little-endian streams.
  **[docs]**
- **Commons Lang3 3.20.0**
  [`Conversion`](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/Conversion.html)`.byteArrayToInt(src, srcPos, dstInit, dstPos, nBytes)` does take
  an offset, but it is little-endian (LSB0) only, signed, and shaped for bit-fiddling generality rather
  than for reading protocol fields. **[docs]**
- **Commons IO** `EndianUtils.readSwappedInteger(byte[], offset)` and `readSwappedUnsignedInteger`
  (→ `long`) take offsets and have an unsigned variant, but "swapped" means little-endian only:
  big-endian is assumed to be the JDK's job. **[docs]**
- **Apache POI** [`LittleEndian`](https://poi.apache.org/apidocs/4.1/org/apache/poi/util/LittleEndian.html)`.getUShort(byte[], offset)` → `int` and `getUInt` → `long` are exactly
  the right shape — offset-based, genuinely unsigned (widened) — and little-endian only, inside a
  multi-megabyte Office-document library. **[docs]**
- **Bouncy Castle** `org.bouncycastle.util.Pack`: `bigEndianToInt(bs, off)`,
  `littleEndianToLong(bs, off)`, `intToBigEndian(n, bs, off)` — both endians, offset-based, zero
  ceremony, heavily exercised by every cipher in the library. Signed only, and a public-but-really-
  internal helper of a crypto library. **[docs]**

## R_korlibs_memory — korlibs `korlibs-memory` (KorGE)

- [`ByteArray.getU16LE(o)`](https://docs.korge.org/memory/), `getS32BE(o)` and friends: offset access, both endians, signed *and*
  unsigned variants, 24-bit, floats, multiplatform — the closest match in shape to a byte-array
  accessor library. **[docs]**
- The unsigned variants **widen**: `getU16LE` returns `Int`, not `UShort`. **[docs]**
- The standalone `kmem` artifact is archived; the functionality now ships inside KorGE's
  `korge-foundation`, so using it means pulling a game engine's foundation module. **[docs]**
- Mid-deprecation (`readU16LE` → `getU16LE`), so the API is moving. **[docs]**

## R_format_parsers — declarative binary-format parsers

- [Kaitai Struct](https://kaitai.io/), JBBP and friends describe a format in a schema and generate the
  parsing code; the generated parser, not the caller, does the offset arithmetic. **[docs]**
- They target large, deeply nested, versioned binary formats, and carry a schema compiler and a
  generated-code step to match. **[docs]**
