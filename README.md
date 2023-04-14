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
    compile("com.gitlab.mvysny.kotlin-unsigned-jvm:kotlin-unsigned-jvm:0.1")
}
```

## Motivation

Why yet another library when you can use `DataInputStream.read*()` and `DataOutputStream.write*()` functions?
Two reasons:

* Those classes do not support unsigned Kotlin types directly;
* Those classes always use `Endian.Big`.

What about Kotlin's built-in [ByteArray.setUIntAt()](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.native/set-u-int-at.html)?

* It's only for Kotlin/Native, it's not available in Kotlin/JVM
* The endianness is undocumented, which is a big issue.

## Releasing

To release the library to Maven Central:

1. Edit `build.gradle.kts` and remove `-SNAPSHOT` in the `version=` stanza
2. Commit with the commit message of simply being the version being released, e.g. "1.2.13"
3. git tag the commit with the same tag name as the commit message above, e.g. `1.2.13`
4. `git push`, `git push --tags`
5. Run `./gradlew clean build publish`
6. Continue to the [OSSRH Nexus](https://oss.sonatype.org/#stagingRepositories) and follow the [release procedure](https://central.sonatype.org/pages/releasing-the-deployment.html).
7. Add the `-SNAPSHOT` back to the `version=` while increasing the version to something which will be released in the future,
   e.g. 1.2.14, then commit with the commit message "1.2.14-SNAPSHOT" and push.

# License

See [LICENSE](LICENSE)
