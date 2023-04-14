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

TODO
