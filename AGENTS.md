# kotlin-unsigned-jvm — AGENTS.md

## What this is

A tiny, dependency-free Kotlin/JVM library that mimics Dart's `ByteData`: extension functions on
`ByteArray` that read and write signed and unsigned 8/16/32/64-bit integers and IEEE-754 floats at a
byte offset, with explicit endianness. The JDK owns the byte shuffling (byte-array-view `VarHandle`s);
this library owns the API shape — true Kotlin unsigned types, endianness as an ordinary parameter, no
wrapper object.

## Promises

- **True unsigned types, at every width.** Every width has an accessor returning `UByte`/`UShort`/`UInt`/`ULong`; the caller never writes the `.toUInt()` it could forget.
- **`endian` is an ordinary argument, defaulting to `Endian.Big`** — never buffer state, never baked into a name, never platform-dependent.
- **No wrapper, no dependencies.** A plain `ByteArray` in, a value out; nothing to allocate, thread through, or keep in sync with the array.
- **The bits are never rewritten.** `setFloat`/`setDouble` write exactly the bits they are handed, so a NaN keeps its payload.

## Design docs

| File | Owns | Loaded |
|---|---|---|
| `README.md` | the pitch, the accessor list, the coordinates, why not `DataInputStream` / `ByteBuffer` | — |
| `AGENTS.md` (this) | promises, invariants, the module map, conventions, commands | every turn |
| `design/architecture.md` | how the pieces compose — the delegation chain, how the JPMS module is assembled, where to start reading; normative | lazy |
| `design/decisions.md` | why this and not that — `D_` entries, FAQ-shaped | lazy |
| `design/research.md` | what the alternatives actually do — `ByteBuffer`, `VarHandle`, kotlinx-io, Okio, korlibs, Commons — `R_` entries, each claim with provenance | lazy |
| `design/ideas/` | not yet decided — one file per idea, `ls` is the index | transient |
| KDoc in `src/main/kotlin/` | what one accessor returns, its offset contract, what it throws | at the symbol |

Every fact lives in exactly one of these; the others link to it.

## Invariants

- **The six byte-array-view `VarHandle`s stay top-level `private val`s in `Endian.kt`** — inside the enum they compile, pass every test, and are slower than the shifts they replaced. See `D_varhandle`.
- **`setFloat`/`setDouble` write through `toRawBits()`, never `toBits()`** — and NaN assertions go through `.toRawBits()`, since `kotlin.test.expect` canonicalizes too. See `D_float_raw_bits`.
- **The JPMS module name, its `exports` and the `--patch-module` argument all name `com.github.mvysny.unsigned`** — when they drift javac says "package is empty or does not exist", and the tempting fix is an empty `Dummy.java`. See `D_patch_module`.
- **The `-javadoc.jar` is filled from `dokkaGeneratePublicationHtml`; the `javadoc` task stays disabled** — every other way of filling it ships a broken jar, and nothing fails when it does. See `D_dokka_html`.

## Module map

Single source set, one package, no nested `AGENTS.md`. One line per file:

- `src/main/kotlin/Endian.kt` — the enum; all byte shuffling, over six `VarHandle`s.
- `src/main/kotlin/ByteArrays.kt` — the public `ByteArray.getX/setX` extensions; one-line delegates, no logic.
- `src/main/kotlin/Parts.kt` — `UShort.hibyte` / `UShort.lobyte`.
- `src/main/java/module-info.java` — the JPMS descriptor; the only Java source.
- `src/test/kotlin/TestUtils.kt` — `ByteArray.toHex()`, `Byte.toHex()`, `String.fromHex()`; tests express bytes as hex, so reuse these.

## Conventions

- **`kotlin { explicitApi() }` is on.** Every public declaration needs an explicit `public`, a return type and — by project convention — a KDoc block; that KDoc is what Dokka publishes.
- **A wider-typed setter overload silently ignores the high bits** — `setShort(Int)`, `setUShort(UInt)`. Documented behaviour, not a bug.
- **Files that `inline` trivial functions carry `@file:Suppress("NOTHING_TO_INLINE")`.**
- **JVM target is 17** for both Kotlin and Java; raising it means updating the CI matrix too.
- **Tests are JUnit 5 with `kotlin.test.expect`**, one `@Nested inner class` per operation and one `@Test` per value — so filter with the `Outer$Inner` form.
- **Hex literals ≥ 2^63 can't be written as `ULong`** (KT-4749); tests use `"deadbeef…".toULong(16)` in a top-level `private val` instead.
- **No linter and no formatter** are configured; match the file you are in.
- **`bin/`, `.classpath`, `.project`, `.settings/`, `build/`, `.gradle/`** are Eclipse and Gradle output, git-ignored, never sources — ignore them when searching.

## Commands

- `./gradlew` — `defaultTasks = clean build`: compiles, runs tests, builds the jars.
- `./gradlew test` — tests only. Failed-test stack traces already print to stdout (`exceptionFormat = FULL`); no need for `--info` / `--stacktrace`.
- `./gradlew test --tests 'com.github.mvysny.unsigned.EndianTest$Little*'` — one nested class; `…PartsTest$UShort.hibyte` — one method.
- `./gradlew dokkaGeneratePublicationHtml` — API docs into `build/dokka/html/`; this fills the published jar.
- `design/verify_design_tripwires.sh && design/verify_project_tripwires.sh` — the doc layer, then this project's own mechanical checks; run both before committing a doc change.
- CI (`.github/workflows/gradle.yml`) runs `./gradlew clean build` on JDK 17/21/24 × Linux/macOS/Windows, plus both tripwire scripts once on Linux.
- Releasing: see `CONTRIBUTING.md`.

## Skills this project follows

- **KDoc states each fact at the level it belongs:** a contract shared by every accessor sits once on the `Endian` class doc, a per-symbol fact on the symbol — members are deliberately not standalone; the `writing-kdoc` skill has the rules, `D_kdoc_voice` what this project decided on top of them.
- **An idea graduates by moving each nugget to the file above that owns it, then deleting the idea file** — it is a scratchpad, held to no doc rule; the `ideas-folder` skill has the procedure.

## Maintenance of this file

Loaded every turn; cap 34 KB, a module's own `AGENTS.md` 10 KB. Over it, in this order:
delete what has no home — status, history, class lists, what the code already says; trim
each line to its fact plus one clause and send the explanation home — why →
`design/decisions.md`, how across symbols → `design/architecture.md`, how in one symbol →
its doc comment, what upstream does → `design/research.md`; only then a module's own
`AGENTS.md`, peripheral modules first, never the core. Never paraphrase a lazy entry into a
line here. `design/verify_design_tripwires.sh` checks the caps and the cites.
