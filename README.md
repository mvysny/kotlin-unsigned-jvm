# Kotlin Unsigned utilities for JVM

This library mimics Dart's `ByteData` utility functions and adds the following extension
functions:

* `ByteArray.getByte(byteOffset)`
* `ByteArray.setByte(byteOffset, value)`
* `ByteArray.getUByte(byteOffset)`
* `ByteArray.setUByte(byteOffset, value)`
* `ByteArray.getShort(byteOffset, endian)`
* `ByteArray.setShort(byteOffset, value, endian)`
* `ByteArray.getUShort(byteOffset, endian)`
* `ByteArray.setUShort(byteOffset, value, endian)`
* `ByteArray.getInt(byteOffset, endian)`
* `ByteArray.setInt(byteOffset, value, endian)`
* `ByteArray.getUInt(byteOffset, endian)`
* `ByteArray.setUInt(byteOffset, value, endian)`
* `ByteArray.getLong(byteOffset, endian)`
* `ByteArray.setLong(byteOffset, value, endian)`
* `ByteArray.getULong(byteOffset, endian)`
* `ByteArray.setULong(byteOffset, value, endian)`

The `endian` value always defaults to `Endian.Big`. Supports both `Endian.Little` and `Endian.Big`.
The one-byte functions take no `endian` parameter since endianness is meaningless for a single byte.

The 8-bit and 16-bit setters are overloaded to also accept a wider value type, for convenience:
`setByte()` accepts an `Int`, `setUByte()` a `UInt`, `setShort()` an `Int` and `setUShort()` a `UInt`.
The high bits are silently ignored in that case.

There are also two extension properties for splitting a 16-bit value into bytes:

* `UShort.hibyte` — the highest 8 bits, as a `UByte`
* `UShort.lobyte` — the lowest 8 bits, as a `UByte`

## Using the library in your projects

The library is in Maven Central. The usage is very simple, just add this to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation("com.github.mvysny.kotlin-unsigned-jvm:kotlin-unsigned-jvm:0.3")
}
```

## Motivation

Why yet another library when you can use `DataInputStream.read*()` and `DataOutputStream.write*()` functions?
Two reasons:

* `Data*Stream` do not support unsigned Kotlin types directly;
* `Data*Stream`  always use `Endian.Big`, it's not possible to configure them to write in `Endian.Little`.

What about Kotlin's built-in [ByteArray.setUIntAt()](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.native/set-u-int-at.html)?

* It's only for Kotlin/Native, it's not available in Kotlin/JVM
* The endianness is undocumented, which is a big issue. Could be big, little, or platform-specific.

What about Java's built-in `ByteBuffer`? That's the closest contender by far, and it deserves a fair
hearing. It's not merely a buffer with a pointer: it has had *absolute*, index-based accessors since
Java 1.4, which never touch the position:

```kotlin
val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
val value: UInt = buffer.getInt(4).toUInt()
buffer.putInt(4, (value + 1u).toInt())
```

Yeah.... that also works — bit-for-bit identically to `bytes.getUInt(4, Endian.Little)` — and it
throws in `Float`/`Double`, slicing and off-heap buffers for free. Three differences remain:

* **No unsigned types.** Every read needs a trailing `.toUInt()`/`.toULong()`, and every write a
  `.toInt()`/`.toLong()`. Forget one and a `0xFFFFFFFF` field silently becomes `-1` — a bug rather
  than a compile error. And for unsigned 64-bit there is no widening trick available at all: Kotlin's
  `ULong` is the only answer the JVM has.
* **Endianness is buffer state, not an argument.** A frame with a big-endian header and a
  little-endian payload (more common than you'd hope) means either two wrapper buffers, or `order()`
  calls interleaved with your reads. Here `endian` is an ordinary parameter: store it in a `val`,
  pass it down, or pick it from a header you just parsed.
* **A wrapper object.** `ByteBuffer.wrap()` allocates something you then have to keep alongside — or
  instead of — the array you already have.

If those three don't bother you, use `ByteBuffer`; it's free and it's in the JDK. See
[COMPARISON.md](COMPARISON.md) for the same treatment of `VarHandle`, `MemorySegment`, kotlinx-io,
Okio, Netty, Guava, Apache Commons and others.

Not everything on that list is a competitor. kotlinx-io in particular solves a different shape of
problem — streaming and segmented buffers — and composes with this one rather than replacing it: read
the frame with kotlinx-io, then index into the resulting `ByteArray` with this.
