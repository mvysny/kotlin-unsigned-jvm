# AGENTS.md

Guidance for AI coding agents working in this repository.

## What this is

A tiny, dependency-free Kotlin/JVM library (`com.github.mvysny.kotlin-unsigned-jvm:kotlin-unsigned-jvm`)
published to Maven Central. It mimics Dart's `ByteData`: extension functions on `ByteArray` to read/write
signed and unsigned 8/16/32/64-bit integers at a byte offset with explicit endianness. See README.md for
the motivation (why not `DataInputStream` / `ByteBuffer` / Kotlin-Native `setUIntAt`).

Architecture is deliberately trivial. Read `src/main/kotlin/` in full (three files, ~300 lines) before changing anything.

## Build and test

Gradle wrapper, Kotlin 2.x, JDK 17+ (CI matrix: JDK 17/21/24 on Linux, macOS, Windows).

```bash
./gradlew                       # defaultTasks = clean build (compiles, runs tests, builds jars)
./gradlew test                  # tests only
./gradlew test --tests 'com.github.mvysny.unsigned.EndianTest'                 # one test class
./gradlew test --tests 'com.github.mvysny.unsigned.EndianTest$Little*'         # one @Nested inner class
./gradlew test --tests 'com.github.mvysny.unsigned.PartsTest$UShort.hibyte'    # one test method
```

Tests are JUnit 5 with `kotlin.test.expect`. Test classes lean heavily on `@Nested inner class` grouping
(one nested class per operation, one `@Test` per value), so use the `Outer$Inner` form when filtering.
Failed-test stack traces already print to stdout (`exceptionFormat = FULL`); no need for `--info`/`--stacktrace`.

There is no linter or formatter configured.

## Layout and how the pieces fit

`src/main/kotlin/` (package `com.github.mvysny.unsigned`):

- `Endian.kt` — the enum `Endian { Big, Little }` is where all byte-shuffling lives. Each constant overrides
  the four abstract primitives `getShort/setShort(Int)/getInt/setInt/getLong/setLong`; every unsigned and
  `Short`-typed variant is a non-abstract `inline` wrapper that converts and delegates to those primitives.
  Add a new width or type here first.
- `ByteArrays.kt` — the public `ByteArray.getX/setX(byteOffset, [value], endian = Endian.Big)` extension
  API. Every function is a one-line `inline` delegate to `Endian`; it contains no logic of its own. Byte-sized
  variants (`getByte/setByte/getUByte/setUByte`) bypass `Endian` since endianness is meaningless for one byte.
- `Parts.kt` — `UShort.hibyte` / `UShort.lobyte` extension properties.

`src/main/java/`:

- `module-info.java` declares the JPMS module `com.github.mvysny.unsigned`. `Dummy.java` is an intentionally
  empty package-info-like file; it exists only so `compileJava` runs and the module descriptor gets compiled
  (workaround for KT-55389). Do not delete it, and keep the Java package name identical to the Kotlin one.

`src/test/kotlin/TestUtils.kt` provides `ByteArray.toHex()`, `Byte.toHex()`, `String.fromHex()`; all
tests express expected bytes as hex strings, so reuse these rather than building byte arrays by hand.

## Conventions that matter here

- `kotlin { explicitApi() }` is on: every public declaration needs an explicit `public` modifier and a
  return type, and (by project convention) a KDoc block. Files that use `inline` on trivial functions carry
  `@file:Suppress("NOTHING_TO_INLINE")`.
- Default endianness is `Endian.Big` everywhere; keep that consistent when adding overloads.
- `setShort(Int)`/`setUShort(UInt)`-style overloads that accept a wider type silently ignore the high bits.
  This is documented behaviour, not a bug.
- Hex literals ≥ 2^63 can't be written directly as `ULong` in Kotlin (KT-4749); tests use
  `"deadbeef...".toULong(16)` in a top-level `private val` instead.
- JVM target is 17 for both Kotlin and Java; don't raise it without also updating the CI matrix.

## Releasing

See CONTRIBUTING.md. Short form: drop `-SNAPSHOT` from `version` in `build.gradle.kts`, commit and tag with
the bare version string, push with tags, then `./gradlew clean build publish closeAndReleaseStagingRepositories`,
then bump to the next `-SNAPSHOT`. Publishing goes through the Sonatype Central OSSRH staging API
(`io.github.gradle-nexus.publish-plugin`) and requires signing credentials.

## Repo hygiene

`bin/`, `.classpath`, `.project`, `.settings/` are Eclipse/Buildship output and are git-ignored. `build/`
and `.gradle/` are Gradle output. None of these are sources; ignore them when searching.
