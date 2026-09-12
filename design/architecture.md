# Architecture

The bird's-eye map of the code as it is: the major pieces, how they are wired, and the flows that
cross several of them. **The code and its KDoc are the authority; this file describes them.** When
they disagree, fix this file.

Keep it short. Anything true of one function belongs in that function's KDoc — which
`explicitApi()` requires and Dokka publishes — not here. This file holds only what no single symbol
can: the direction of delegation, how the JPMS module is assembled out of two source sets, and
where a newcomer should start reading. Cite `R_` for a promise and `D_` for why; argue nothing here.
**The first entry in each section is the ruler** — later entries are trimmed to its length, never
the other way round. The module map — one line per file — is in `AGENTS.md`, because an agent needs
it every turn.

---

## Wiring

- Delegation points one way and never back: `ByteArrays.kt` → `Endian` → a `VarHandle`. Nothing in
  `Endian.kt` knows the `ByteArray` extension API exists.
- `ByteArrays.kt` holds no logic at all — every function is a one-line `inline` delegate that picks
  the `Endian` method and supplies `Endian.Big` as the default (`R_endian_defaults_big`).
- `Endian`'s two constants override exactly six primitives (`get`/`set` × short/int/long, the
  `set`s taking `Int`/`Long`). Every other accessor on the enum — the unsigned ones, the
  `Short`-typed `setShort`, the float ones — is a non-abstract `inline` wrapper that converts and
  delegates to those six. A new width or type is added here first, then exposed in `ByteArrays.kt`.
- The float accessors are pure reinterpretation on top of that: `getFloat`/`setFloat` wrap
  `getInt`/`setInt` through `Float.fromBits`/`toRawBits` (`getDouble`/`setDouble` likewise over
  `getLong`/`setLong`), so no byte shuffling is float-aware and neither enum constant mentions them.
  `toRawBits` and not `toBits` (`R_no_nan_canonicalization`, `D_float_raw_bits`).
- The six `VarHandle`s are file-private top-level `val`s, deliberately outside the enum
  (the `AGENTS.md` invariant, `D_varhandle`). They are the only JVM-specific code in the library.
- Byte-sized accessors (`getByte`/`setByte`/`getUByte`/`setUByte`) and `Parts.kt` bypass `Endian`
  entirely; one byte has no byte order.

## Flows

**A read — `bytes.getUInt(4, Endian.Little)`:** `ByteArray.getUInt` inlines into
`Endian.Little.getUInt(this, 4)` → the enum's `inline` wrapper calls `getInt` → `Little`'s override
reads `INT_LE.get(bytes, 4) as Int` → `.toUInt()`. After inlining and C2's constant-folding of the
handle, the whole chain is one unaligned byte-swapping load; the `UInt` value class costs nothing.
A write is the same chain in reverse, with the widening overloads (`setUShort(UInt)`) truncating
silently on the way down.

**Assembling the JPMS module** (`./gradlew build`): `compileKotlin` writes the classes for
`com.github.mvysny.unsigned`; `compileJava` then compiles `src/main/java/module-info.java` *alone*
and would reject its `exports` as an empty package, so `build.gradle.kts` hands javac
`--patch-module com.github.mvysny.unsigned=<kotlin output>`. All three spellings of the package
must match (the `AGENTS.md` invariant, `D_patch_module`). The `-javadoc.jar` is filled from
`dokkaGeneratePublicationHtml`, never from the `javadoc` task, which is disabled
(`D_dokka_html`).

## Where to start reading

`src/main/kotlin/Endian.kt` — the whole library is there; `ByteArrays.kt` is its public surface
restated as extensions, and `Parts.kt` is ten lines. Then `src/test/kotlin/EndianTest.kt` for what
each accessor is expected to produce, byte for byte.
