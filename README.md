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

Why not using Java built-in `ByteBuffer` as follows:

```kotlin
val buffer = ByteBuffer.wrap(ByteArray(10))
buffer.order(ByteOrder.LITTLE_ENDIAN)
buffer.getLong().toULong()
```

Yeah.... that also works. Yet, I'd argue that working with `ByteArray` and indexes is simpler than with a `ByteBuffer` with a pointer.
