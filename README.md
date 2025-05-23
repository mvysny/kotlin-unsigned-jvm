# Kotlin Unsigned utilities for JVM

This library mimics Dart's `ByteData` utility functions and add the following extension
functions:

* `ByteArray.getByte(byteOffset, value)`
* `ByteArray.setByte(byteOffset, value)`
* `ByteArray.getUByte(byteOffset, value)`
* `ByteArray.setUByte(byteOffset, value)`
* `ByteArray.getShort(byteOffset, value, endian)`
* `ByteArray.setShort(byteOffset, value, endian)`
* `ByteArray.getUShort(byteOffset, value, endian)`
* `ByteArray.setUShort(byteOffset, value, endian)`
* `ByteArray.getInt(byteOffset, value, endian)`
* `ByteArray.setInt(byteOffset, value, endian)`
* `ByteArray.getUInt(byteOffset, value, endian)`
* `ByteArray.setUInt(byteOffset, value, endian)`
* `ByteArray.getLong(byteOffset, value, endian)`
* `ByteArray.setLong(byteOffset, value, endian)`
* `ByteArray.getULong(byteOffset, value, endian)`
* `ByteArray.setULong(byteOffset, value, endian)`

The `endian` value always defaults to `Endian.big`. Supports both `Endian.Little` and `Endian.Big`.

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

What about Kotlin's built-in [ByteArray.setUIntAt()](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.native/set-u-int-at.html)?

* It's only for Kotlin/Native, it's not available in Kotlin/JVM
* The endianness is undocumented, which is a big issue. Could be big, little, or platform-specific.

Why not using Java built-in `ByteBuffer` as follows:

```java
ByteBuffer buffer = ByteBuffer.wrap(new byte[10]);
buffer.order(ByteOrder.LITTLE_ENDIAN);
buffer.getLong().toULong();
```

Yeah.... that also works. Yet, I'd argue that working with `ByteArray` and indexes is simpler than with a `ByteBuffer` with a pointer.
