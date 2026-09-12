# How this library compares to the alternatives

An honest survey of the popular open-source (and JDK built-in) ways to read and write
fixed-width integers out of a byte buffer on the JVM, and an answer to the question:
**can `kotlin-unsigned-jvm` retire?**

Short version: **no popular library does exactly what this one does**, but `java.nio.ByteBuffer`
plus a `.toUInt()` call gets you ~90% of the way for free. Whether that 10% is worth a dependency
is the whole decision. See [Verdict](#verdict).

Surveyed as of 2026-09-12.

## The reference snippet

Everything below is measured against one task: *read the unsigned 32-bit little-endian field at
byte offset 4 of a `ByteArray`, then write one back*. In this library:

```kotlin
val v: UInt = bytes.getUInt(4, Endian.Little)
bytes.setUInt(4, v + 1u, Endian.Little)
```

## The axes that matter

Not all of these matter to everyone; which ones you care about decides the answer.

- `A_random_access` — read/write at an **arbitrary byte offset**, repeatedly, in any order.
  The opposite is a consuming cursor (`readInt()` advances a pointer), which is a poor fit for
  fixed-layout protocol frames and record structures where you want to name offsets.
- `A_kotlin_unsigned` — returns Kotlin's `UByte`/`UShort`/`UInt`/`ULong` value classes, rather than
  widening into a signed `Int`/`Long`. Widening is the traditional JVM answer and it works, but it
  loses the type: nothing stops you passing a "really unsigned" `Int` to something that treats it as
  signed, `toString()` prints the wrong thing for the top half of the range, and there is no widening
  trick at all for unsigned 64-bit.
- `A_explicit_endian` — endianness is chosen **per call**, at the call site. The alternative is
  endianness held as state on a buffer object (`ByteBuffer.order()`), or baked into the function name
  (`readIntLe`), or fixed by the library (big-endian only, little-endian only).
- `A_no_wrapper` — operates on a plain `ByteArray`, with no wrapper object to allocate, thread
  through, or keep in sync with the array.
- `A_zero_dep` — brings no transitive dependencies.
- `A_multiplatform` — usable outside Kotlin/JVM.
- `A_floats` — can also read/write IEEE-754 `Float`/`Double`.

## Summary table

| | `A_random_access` | `A_kotlin_unsigned` | `A_explicit_endian` | `A_no_wrapper` | `A_zero_dep` | `A_multiplatform` | `A_floats` |
|---|---|---|---|---|---|---|---|
| **kotlin-unsigned-jvm** | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ JVM only | ❌ |
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

⚠️ = partial; see the section below.

## JDK built-ins

### `java.nio.ByteBuffer` — the real competitor

The README dismisses `ByteBuffer` as "a buffer with a pointer", but that undersells it:
`ByteBuffer` has had **absolute, index-based** accessors since 1.4, which do not touch the position
at all. The pointer is irrelevant if you never use the relative methods.

```kotlin
val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
val v: UInt = bb.getInt(4).toUInt()
bb.putInt(4, (v + 1u).toInt())
```

That is genuinely close. Verified against this library: `bb.getInt(4).toUInt()` is bit-for-bit
identical to `bytes.getUInt(4, Endian.Little)`, `bb.getLong(8).toULong()` to `getULong`, and even
unsigned 16-bit has a JDK trick — `bb.getChar(2)` reads an unsigned 16-bit value, since `Char` *is*
the JVM's unsigned 16-bit type.

What you give up:

- `A_kotlin_unsigned`: every read needs a trailing `.toUInt()` / `.toULong()`, and every write a
  `.toInt()` / `.toLong()`. Not hard, just noise at every call site — and the conversion is easy to
  forget, at which point a `0xFFFFFFFF` field silently becomes `-1`.
- `A_explicit_endian`: order is buffer state, not an argument. A frame that mixes big-endian headers
  with little-endian payload (more common than you'd hope) means either two wrapper buffers or
  `order()` calls interleaved with reads — stateful and easy to get wrong.
- `A_no_wrapper`: `ByteBuffer.wrap()` allocates. Usually escape-analysed away or hoisted out of the
  loop, but it's an object you now have to pass around alongside — or instead of — the array.

`ByteBuffer` also does everything this library doesn't: `Float`/`Double`, bulk `get(byte[])`,
`slice()`, direct/off-heap buffers, and `CharBuffer`/`IntBuffer` views.

### `VarHandle` byte-array views — the fast one

`MethodHandles.byteArrayViewVarHandle` views a `byte[]` as if it were an `int[]`/`long[]`/`short[]`,
and the JIT compiles the access down to a single (possibly byte-swapping) unaligned load:

```kotlin
val INT_LE = MethodHandles.byteArrayViewVarHandle(IntArray::class.java, ByteOrder.LITTLE_ENDIAN)
val v = (INT_LE.get(bytes, 4) as Int).toUInt()
```

Verified: this works from Kotlin, and plain `get`/`set` accept misaligned offsets (offset 5 into an
int view is fine — only the atomic access modes require alignment). This is what Lucene and friends
use, and it is the performance ceiling for this kind of code.

The ergonomics are poor, though: the handle is untyped (`as Int` at every call site, with no compile-time
check that you picked the right cast), the endianness is baked into the handle rather than passed, so
you need one static handle per (width × order) — six of them to match this library's coverage — and
Kotlin's support for signature-polymorphic calls has had compiler bugs as recently as
[KT-72880](https://youtrack.jetbrains.com/issue/KT-72880) (fixed in Kotlin 2.4.0).

If you have a measured hot loop, reach for this. For everything else the ergonomics aren't worth it.

### `MemorySegment` (JDK 22+)

The modern successor, and where the JDK is heading:

```kotlin
val seg = MemorySegment.ofArray(bytes)
val layout = ValueLayout.JAVA_INT_UNALIGNED.withOrder(ByteOrder.LITTLE_ENDIAN)
val v = seg.get(layout, 4L).toUInt()
```

Endianness travels with the layout rather than the buffer, which is nicer than `ByteBuffer`. Still
signed-only, still a wrapper, and it needs JDK 22 — this library targets JDK 17, so for most users
today it isn't an option yet. Worth revisiting once 25 is the floor.

### `DataInputStream` / `DataOutputStream`

Covered in the README, and the assessment there still holds: stream-shaped, big-endian only, no
unsigned. Not a competitor for random access.

## Kotlin stdlib

`ByteArray.getIntAt()` / `setUIntAt()` and friends exist — but only in `kotlin.native`, only under
`@ExperimentalNativeApi`, and, confirmed again against the current 2.4 docs, **the byte order is still
undocumented**. There is no common or JVM equivalent, and no visible plan to add one. The README's
complaint is accurate and has not aged.

So: the Kotlin stdlib is not going to make this library redundant any time soon.

## Kotlin/JVM I/O libraries

### kotlinx-io (0.9.1)

The closest thing to an "official" answer, and the only surveyed library that returns **real Kotlin
unsigned types**: `readUByte`, `readUShort`/`readUShortLe`, `readUInt`/`readUIntLe`,
`readULong`/`readULongLe`, with matching `writeU*`. Multiplatform.

But it is a `Source`/`Sink` **cursor** API — reads are consuming, and there is no random access to a
multi-byte value at an arbitrary offset. Endianness lives in the function name, so switching it based
on a parsed header means a `when`. And it's still pre-1.0, so the API can move.

Different shape of problem: kotlinx-io is for streaming and segmented buffers; this library is for
poking at a fixed-layout frame you already have in memory. They compose fine — read the frame with
kotlinx-io, then index into it with this.

### Okio (3.18.2)

Mature, enormously popular, multiplatform. `readInt`/`readIntLe`, `readShort`/`readShortLe`,
`readLong`/`readLongLe` — **all signed**, no unsigned variants, despite Okio being Kotlin-first.
Cursor-based like kotlinx-io; `Buffer` offers indexed access for single bytes only. Same verdict: a
different job.

### Netty `ByteBuf` (4.2)

The most complete *random-access* unsigned API on the JVM:

```kotlin
val buf = Unpooled.wrappedBuffer(bytes)
val v: Long = buf.getUnsignedIntLE(4)   // absolute index, doesn't move readerIndex
```

Absolute-index `getUnsignedByte`/`getUnsignedShort`/`getUnsignedMedium`/`getUnsignedInt`, each with an
`LE` twin, plus 24-bit medium support this library lacks. But: results are **widened** (`short`, `int`,
`long`) rather than Kotlin unsigned types; there is **no unsigned 64-bit** at all, because Java has no
type for it; endianness is in the method name; and you inherit reference counting, pooling, and a
non-trivial dependency. Sensible if you're already in Netty, absurd otherwise.

## General-purpose utility libraries

### Guava

`Ints.fromByteArray(byte[])` and `Longs.fromByteArray(byte[])` are **big-endian only and read from
offset 0** — there is no offset parameter, so reading a field at offset 4 means `Ints.fromBytes(b[4],
b[5], b[6], b[7])` or an array copy. `UnsignedInts`/`UnsignedLongs`/`UnsignedBytes` help with
*arithmetic* on widened values (compare, divide, `toString`), which is real value Kotlin gets for free
from its unsigned types. `LittleEndianDataInputStream` covers little-endian streams. Not a random-access
byte-array API.

### Apache Commons Lang3 `Conversion` (3.20.0)

`Conversion.byteArrayToInt(src, srcPos, dstInit, dstPos, nBytes)` does support offsets — but it's
little-endian (LSB0) only, signed, and the five-parameter signature speaks for itself. It exists for
bit-fiddling generality, not for reading protocol fields.

### Apache Commons IO `EndianUtils`

`readSwappedInteger(byte[], offset)` and `readSwappedUnsignedInteger(byte[], offset)` (→ `long`).
Offsets, and an unsigned variant — but "swapped" means **little-endian only**; big-endian is assumed to
be the JDK's job. Half the matrix, signed-or-widened.

### Apache POI `LittleEndian`

`LittleEndian.getUShort(byte[], offset)` → `int`, `getUInt(byte[], offset)` → `long`. Exactly the right
shape, genuinely unsigned (widened), offset-based — and little-endian only, living inside a
multi-megabyte Office-document library. Nobody should take on POI for this.

### Bouncy Castle `org.bouncycastle.util.Pack`

`bigEndianToInt(bs, off)`, `littleEndianToLong(bs, off)`, `intToBigEndian(n, bs, off)` — both endians,
offset-based, zero ceremony, and heavily exercised by every cipher in the library. Signed only, and
it's a crypto library's public-but-really-internal helper; taking a dependency on `bcprov` for byte
shuffling is not a defensible engineering choice.

### korlibs `korlibs-memory` (KorGE)

`ByteArray.getU16LE(o)`, `getS32BE(o)` etc. — random access, both endians, signed *and* unsigned
variants, plus 24-bit and float support, multiplatform. The closest match in shape to this library.
Two problems: unsigned variants **widen** (`getU16LE` returns `Int`, not `UShort`), and the standalone
`kmem` artifact is archived — the functionality now ships inside KorGE's `korge-foundation`, so you're
pulling a game engine's foundation module into a server-side project. Also mid-deprecation
(`readU16LE` → `getU16LE`), so the API is moving.

## A different category

Declarative binary-format parsers — [Kaitai Struct](https://kaitai.io/), JBBP, and friends — describe
a format in a schema and generate parsing code. If your input is a large, deeply-nested, versioned
binary format, that is the better tool by far, and neither this library nor `ByteBuffer` is really
competing with it. For a 20-byte Modbus/RS-485/BLE frame it is wildly disproportionate.

## Where this library wins

Only one cell in that table is uniquely filled: **random access into a plain `ByteArray`, returning
real Kotlin unsigned types, with endianness as an ordinary argument, and no dependency**. Nothing
popular offers all four.

Concretely, this is what the difference looks like for a mixed-endian protocol frame:

```kotlin
// this library
val id      = frame.getUShort(0, Endian.Big)      // header is network order
val voltage = frame.getUInt(4, Endian.Little)     // payload is little-endian
val serial  = frame.getULong(8, Endian.Little)

// ByteBuffer
val bb = ByteBuffer.wrap(frame)
val id      = bb.getChar(0).code.toUShort()       // or getShort(0).toUShort()
bb.order(ByteOrder.LITTLE_ENDIAN)                 // mutate buffer state mid-parse
val voltage = bb.getInt(4).toUInt()
val serial  = bb.getLong(8).toULong()
```

Secondary points: everything is `inline`, so there's no call overhead and no wrapper allocation;
it's ~300 lines with a JPMS module descriptor and no transitive baggage; and the `Endian` value being
an ordinary argument means you can store it in a `val`, pass it down, or pick it from a parsed header
without restructuring anything.

## Where it loses

- **No `Float`/`Double`.** Dart's `ByteData`, `ByteBuffer`, `MemorySegment` and korlibs all have
  `getFloat32`/`getFloat64`. This is the most obvious real gap, and the cheapest to close
  (`Float.fromBits(getInt(...))`).
- **No 24-bit.** Netty and korlibs both have it; 24-bit fields do turn up in protocol work.
- **JVM only.** kotlinx-io, Okio and korlibs are all multiplatform. If you ever want this code on
  Native or JS, this library is a dead end — and the `-jvm` in the artifact name commits to that.
- **Bus factor of one.** A single-maintainer micro-library is a real, if small, supply-chain
  consideration next to `ByteBuffer` (free, forever) or Okio.
- **Nothing else.** No slicing, no bulk copy, no varints, no strings, no off-heap. It is deliberately
  one thing, which is a virtue right up until you need the second thing and reach for `ByteBuffer`
  anyway.

## Verdict

**Keep it, but know what it's buying.**

The case for retiring it is real and worth stating plainly: `ByteBuffer.wrap(bytes).order(...)` plus a
`.toUInt()` is free, in the JDK, battle-tested, faster to reach for, and covers floats too. If you're
comfortable writing that conversion at every call site and keeping `order()` state straight, you do
not need this library, and "retire it, use `ByteBuffer`" is a defensible answer.

The case for keeping it is that no alternative fills all four cells at once, and the ones that come
close each fail on something structural rather than cosmetic:

- `ByteBuffer` / `VarHandle` / `MemorySegment` — right shape, but signed, and the `.toUInt()` you
  forget is a silent bug rather than a compile error.
- kotlinx-io — right *types*, wrong *shape* (cursor, not random access), and pre-1.0.
- Okio — no unsigned at all.
- Netty / POI / Bouncy Castle / korlibs — right-ish API buried in a dependency nobody would take on
  for this alone.
- Kotlin stdlib — Native-only, experimental, undocumented endianness. Not coming to the JVM.

And the maintenance cost is close to zero: three files, no dependencies, no runtime behaviour to rot,
nothing to track but the JDK baseline and the Kotlin version.

If you do keep it, the changes that would most improve the case for its existence, in order:

1. **Add `getFloat`/`setFloat`/`getDouble`/`setDouble`.** Closes the most-cited gap for a few lines
   of `Float.fromBits` / `toRawBits` delegation, and completes the Dart `ByteData` parity the README
   claims.
2. **Reconsider the JVM-only framing.** The logic is pure Kotlin with no JVM API in it — this could be
   a multiplatform library almost verbatim, which would make it the only random-access unsigned
   `ByteArray` API in Kotlin that returns proper unsigned types on every platform. That's a much
   stronger niche than "JVM-only convenience over `ByteBuffer`".
3. **Say all this in the README.** The current motivation section argues against `Data*Stream`,
   Kotlin/Native and a strawman `ByteBuffer` (relative reads with a pointer), but doesn't address
   `ByteBuffer`'s absolute accessors, which are the genuine competitor. Making the unsigned-typing
   argument explicitly — "`.toUInt()` at every call site is a bug waiting to happen" — is a stronger
   pitch than the ergonomic one.
4. **Add 24-bit accessors** if protocol work is the target use case.

## Sources

- [ByteBuf (Netty 4.2 API)](https://netty.io/4.2/api/io/netty/buffer/ByteBuf.html)
- [MethodHandles.byteArrayViewVarHandle](https://learn.microsoft.com/en-us/dotnet/api/java.lang.invoke.methodhandles.bytearrayviewvarhandle)
- [kotlinx-io core API](https://kotlinlang.org/api/kotlinx-io/kotlinx-io-core/kotlinx.io/)
- [kotlinx-io `readUIntLe`](https://kotlinlang.org/api/kotlinx-io/kotlinx-io-core/kotlinx.io/read-u-int-le.html)
- [Okio `Buffer` source](https://github.com/square/okio/blob/master/okio/src/commonMain/kotlin/okio/Buffer.kt)
- [Kotlin stdlib `kotlin.native.getShortAt`](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.native/get-short-at.html)
- [Apache Commons Lang3 `Conversion`](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/Conversion.html)
- [Apache POI `LittleEndian`](https://poi.apache.org/apidocs/4.1/org/apache/poi/util/LittleEndian.html)
- [korlibs memory reference](https://docs.korge.org/memory/)
- [korlibs `readU16LE`](https://dokka.korge.org/korge-foundation/korlibs.memory/read-u16-l-e.html)
- [Dart `ByteData.getInt32`](https://api.dart.dev/dart-typed_data/ByteData/getInt32.html)
