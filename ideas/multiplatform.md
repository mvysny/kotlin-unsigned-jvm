# Go multiplatform

## Why this is the strongest move available

Today the honest pitch is "a nicer `ByteBuffer` for people who want Kotlin unsigned types" — real,
but narrow, and one `.toUInt()` away from being unnecessary (see COMPARISON.md).

Multiplatform changes the pitch entirely. Nothing in Kotlin currently offers **random access into a
plain `ByteArray`, returning true Kotlin unsigned types, on every platform**:

- **kotlinx-io** has the unsigned types (`readUInt`/`readUIntLe`, etc.) but is a consuming
  `Source`/`Sink` cursor — no random access to a multi-byte value at an arbitrary offset. Also still
  pre-1.0.
- **Okio** has no unsigned API at all.
- **korlibs** (`getU16LE(o)` and friends) has the right *shape* but widens to `Int`/`Long` instead of
  returning `UShort`/`UInt`, and now ships inside KorGE's `korge-foundation` rather than as a
  standalone artifact.
- **Kotlin/Native stdlib** has `getIntAt`/`setUIntAt`, but `@ExperimentalNativeApi`, and — still true
  as of the 2.4 docs — **the endianness is undocumented**. So even on the one platform where the
  stdlib covers this, it doesn't really.

That's an empty niche, not a crowded one. It's also the niche where `ByteBuffer` — our only serious
competitor — simply doesn't exist.

## The code is nearly ready

`ByteArrays.kt` and `Parts.kt` are pure Kotlin — `ByteArray` indexing, shifts, and
`toUByte()`/`toULong()` conversions — and move to `commonMain` verbatim.

`Endian.kt` no longer does (it did until 2026-09-12). Its six primitives now delegate to
`java.lang.invoke` byte-array-view `VarHandle`s, worth 3–5.6× on reads; see `D_varhandle` in
DECISIONS.md. So `Endian` becomes the one `expect`/`actual` split: the `VarHandle` version stays as the
`jvmMain` actual, and `commonMain` gets back the shift-or arithmetic git still has at `6d611c1`. That is
exactly the shape kotlinx-io uses, so it's well-trodden rather than novel — but it does mean the
migration is no longer a pure file move, and the shift-or path needs its own test run per target.

The JVM-only parts are otherwise all build machinery:

- `src/main/java/module-info.java` (the JPMS module), which belongs to the JVM target only. Need to work
  out how it fits a KMP `jvm()` target — this is the fiddliest part of the whole change.
- `tasks.compileJava { options.javaModuleVersion, --patch-module }` in `build.gradle.kts`. The
  `--patch-module` path points at `sourceSets.main.output` and would have to follow the Kotlin output
  wherever a `jvm()` target puts it (see `D_patch_module` in DECISIONS.md).

## The artifact-naming problem — decide this first

Current coordinates:

```
group      com.github.mvysny.kotlin-unsigned-jvm
artifactId kotlin-unsigned-jvm
```

A KMP build appends the target name to the artifactId, so the JVM artifact would publish as
**`kotlin-unsigned-jvm-jvm`**, with `kotlin-unsigned-jvm` becoming the root metadata module. That's
embarrassing enough to be a blocker, and the `-jvm` in both the group *and* the artifact is baked
into every existing user's build file.

Options, none of them free:

1. **New coordinates** (`com.github.mvysny.kotlin-unsigned:kotlin-unsigned`) and leave the JVM-only
   artifact frozen at its last release. Clean, but it's a rename — README, CONTRIBUTING, the
   publishing block and every downstream build file. Probably the right answer.
2. **Keep the coordinates, override the JVM artifactId** so the `-jvm` target publishes under the
   existing `kotlin-unsigned-jvm` name and the metadata module gets a new one. Preserves existing
   users' build files exactly, at the cost of a permanently confusing group id.
3. **Publish the multiplatform library alongside** under new coordinates and keep releasing the JVM
   one as a thin deprecated alias for a version or two.

## Rest of the work

- Target set: `jvm()`, `js()`, `wasmJs()`, and the native tiers. Since the code is arithmetic-only
  with no platform APIs, the marginal cost of each extra target is near zero — but **JS is the one
  to actually think about**: numbers are doubles, `Long`/`ULong` are emulated, and the 64-bit paths
  deserve real tests there rather than an assumption that they work.
- `explicitApi()` stays on and already passes; no API changes needed.
- CI (`.github/workflows/gradle.yml`) is currently a 3 OS × 3 JDK matrix running `clean build`. Needs
  to grow native/JS test runs — macOS for the Apple targets, and `--info --stacktrace` should go
  while we're in there (the build already sets `exceptionFormat = FULL`, so it's pure noise).
- README's `-jvm`-flavoured framing ("Kotlin Unsigned utilities for JVM", the `DataInputStream` and
  `ByteBuffer` arguments) becomes a JVM *section* rather than the whole motivation — on Native and JS
  the competition is entirely different, and mostly absent.

## Open questions

- Is the JVM still the only platform *you* actually ship this on? If so, is the wider niche worth a
  rename and a CI matrix that's several times bigger? The idea is strongest as "own an empty niche",
  which is a positioning argument, not a need-driven one.
- Does going multiplatform make [[float-double-accessors]] harder? `Float.fromBits`/`toRawBits` are
  common stdlib, so no — but NaN bit-pattern preservation on JS is worth a test, since that's exactly
  where a double-backed number representation could bite.
- If this happens, does kotlinx-io become a *collaborator* rather than a competitor — read the frame
  with kotlinx-io, index into it with this? Worth saying so explicitly in the README either way; it's
  true today on the JVM too.
