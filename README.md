# Kotlin Unsigned utilities for JVM

This library mimics Dart's `ByteData` utility functions and add the following extension
functions:

* `ByteArray.setShort(byteOffset, value, endian)`
* `ByteArray.setUShort(byteOffset, value, endian)`

The `endian` value always defaults to `Endian.big`. Supports both `Endian.Little` and `Endian.Big`.
