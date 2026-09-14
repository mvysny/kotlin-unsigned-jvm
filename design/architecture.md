# Architecture

How the pieces compose — what no single symbol can say: the direction of delegation, how the JPMS
module is assembled out of two source sets, and where a newcomer should start reading.
**Normative: the code conforms.** Change this file first, then the code. Not here: why
(`decisions.md` — cite the `D_`), what the alternatives do (`research.md` — cite the `R_`), one
function's behaviour (its KDoc, which `explicitApi()` requires and Dokka publishes), the module map
(`AGENTS.md`, because an agent needs it every turn). **The first entry in each section is the
ruler** — later entries are trimmed to its length. Cap 12 KB — over it, research or KDoc content has
crept in.

---

## Wiring

- Delegation points one way and never back: `ByteArrays.kt` → `Endian` → a `VarHandle`. Nothing in
  `Endian.kt` knows the `ByteArray` extension API exists.
- `ByteArrays.kt` holds no logic at all — every function is a one-line `inline` delegate that picks
  the `Endian` method and supplies `Endian.Big` as the default (the *`endian` is an ordinary
  argument* promise).
- `Endian`'s two constants override exactly six primitives (`get`/`set` × short/int/long, the
  `set`s taking `Int`/`Long`). Every other accessor on the enum — the unsigned ones, the
  `Short`-typed `setShort`, the float ones — is a non-abstract `inline` wrapper that converts and
  delegates to those six. A new width or type is added here first, then exposed in `ByteArrays.kt`.
- The float accessors are pure reinterpretation on top of that: `getFloat`/`setFloat` wrap
  `getInt`/`setInt` through `Float.fromBits`/`toRawBits` (`getDouble`/`setDouble` likewise over
  `getLong`/`setLong`), so no byte shuffling is float-aware and neither enum constant mentions them.
  `toRawBits` and not `toBits` (the *bits are never rewritten* promise, `D_float_raw_bits`).
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
