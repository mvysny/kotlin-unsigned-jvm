# kotlin-unsigned-jvm — AGENTS.md

An index, not a manual: each entry is the one-line invariant ("what you must not break") plus
pointers to where the truth and its rationale live — a KDoc block (`See {Endian}`), a decision
(`See D_<slug>`), a requirement (`See R_<slug>`). Never the explanation itself. **Size cap: 34 KB.** Over it,
garbage-collect by moving, never summarising: a rationale to its `D_`, a per-symbol rule to its
KDoc, a cross-symbol flow to `design/architecture.md`, a directory's own invariants and file map to
that directory's `AGENTS.md` (`ls */AGENTS.md` for the set; ≤ 10 KB each, loaded only when work
touches the directory — this project has none). `CLAUDE.md` is exactly `@AGENTS.md`.

## What this is

A tiny, dependency-free Kotlin/JVM library (`com.github.mvysny.kotlin-unsigned-jvm:kotlin-unsigned-jvm`)
published to Maven Central. It mimics Dart's `ByteData`: extension functions on `ByteArray` that
read and write signed and unsigned 8/16/32/64-bit integers at a byte offset with explicit
endianness. The JDK owns the byte shuffling (byte-array-view `VarHandle`s); this library owns the
API shape — true Kotlin unsigned types, endianness as an ordinary parameter, no wrapper object.

Architecture is deliberately trivial: three Kotlin files, ~300 lines. Read `src/main/kotlin/` in
full before changing anything.

## Design docs

Rationale and reference live under `design/`; this file holds only what its header says it may.
Each file has one audience and *what it is allowed to own*; every file's preamble states its
entry shape and how to cite it. This section is the whole contract — nothing outside the repo
is needed to follow it.

| File | Owns | Loaded? |
|---|---|---|
| `README.md` | a prospective user: what the library does, the coordinates, why not `DataInputStream` / `ByteBuffer` | — |
| `AGENTS.md` (this) | what you must not break from a distance; the module map; this table — its rules are in its header | **every turn** |
| `design/requirements.md` | what must hold — `R_` entries, stated not argued | lazy |
| `design/architecture.md` | **the map** of the code as it is — delegation direction, the read/write chain, how the JPMS module is assembled; **the code is the truth** | lazy |
| `design/decisions.md` | why this and not that — `D_` entries, roads not taken | lazy |
| `design/comparison.md` | what the *alternatives* do — `ByteBuffer`, kotlinx-io, Okio, korlibs, Commons — descriptively, on `A_`-slugged axes | lazy |
| `design/ideas/` | not yet decided — one file per idea, `ls` is the index, deleted on graduation | transient |
| KDoc in `src/main/kotlin/` | per-symbol truth: what each accessor returns, its offset contract, what it throws | source of truth |

Rules that keep the split from drifting:

- **One home per fact; the others link.** A one-line restatement that saves a jump is fine —
  repeat the *fact*, defer the *explanation*. Compressing a `D_` entry into a bullet here is a
  third copy, not a summary.
- **`decisions.md` argues, `requirements.md` states, `comparison.md` is about *them* not us,
  `architecture.md` composes and never argues.** A paragraph explaining *why* in any file but
  `decisions.md` has drifted; move it and cite the `D_`.
- **No `D_` entry without a real fork; only decisions already taken.** Ideas, TODOs and open
  questions go to `design/ideas/`. A shipped decision that is reversed keeps its entry as a
  tombstone.
- **Slugs:** `D_` decisions, `R_` requirements, `T_` tripwires (cited from the requirement's
  *Enforced by*, defined by the check), `Q_` open questions inside `design/ideas/` only — a durable
  doc never cites a `Q_`. This project declares one more: **`A_` comparison axes**, defined and
  used only in `design/comparison.md`. Underscores throughout, backticked in prose, cited by slug
  never by position; `grep '^## D_' design/decisions.md` is the index.
- **`design/verify_design_tripwires.sh`** fails on any cited `D_` / `R_` without a heading, a `T_`
  without a check, an oversized `AGENTS.md`, or a `CLAUDE.md` that isn't the shim;
  **`design/verify_project_tripwires.sh`** holds this project's own `T_` checks. CI runs both; run
  them before committing a doc change.

### Ideas & their graduation

An idea graduates the moment it is acted on, and graduation is not done until its file is gone.
An idea file is a scratchpad, exempt from the doc-quality rules above because it is going to be
deleted. Where the lasting nuggets land:

- the choice made + the alternatives rejected → a `D_` entry in `design/decisions.md`
- something that must hold from now on → an `R_` entry in `design/requirements.md`
- a new file, or a changed responsibility → one line in the module map below
- how the pieces work together — the delegation chain, a flow crossing several files → `design/architecture.md`
- what a competing library or built-in does → `design/comparison.md`, on its `A_` axes
- what one function does, its contract, its edge cases → its KDoc; `explicitApi()` requires one anyway
- usage, motivation, why you'd want this library → `README.md`
- the release process → `CONTRIBUTING.md`
- a cross-cutting invariant ("never …") → this file
- work deferred *as a consequence of a logged decision* → that entry's *Consequences*

*Layout seeded from the `design-docs` and `agents-md` skills (mvysny, `~/.claude/skills`); this
project needs nothing from them.*

## Invariants

- **The six byte-array-view `VarHandle`s stay top-level `private val`s in `Endian.kt`.** Folding
  them into the enum as instance fields compiles, passes every test, and silently makes every
  accessor slower than the shift-or code they replaced. See `R_varhandles_top_level`, `D_varhandle`.
- **`setFloat`/`setDouble` write through `toRawBits()`, never `toBits()`.** The latter rewrites every
  NaN to the canonical pattern, which is non-conformance for every format that specifies bits — and
  the swap is invisible under test, because `kotlin.test.expect` canonicalizes too. Assert NaNs via
  `.toRawBits()`. See `R_no_nan_canonicalization`, `D_float_raw_bits`.
- **The JPMS module name, its `exports` and the `--patch-module` argument all name
  `com.github.mvysny.unsigned`.** When they drift javac says "package is empty or does not exist",
  and the tempting fix is an empty `Dummy.java` — which lies. See `R_module_package_sync`,
  `D_patch_module`.

## Module map

Single source set; no nested `AGENTS.md`. One line per file:

- `src/main/kotlin/Endian.kt` — the enum; all byte shuffling, over six `VarHandle`s.
- `src/main/kotlin/ByteArrays.kt` — the public `ByteArray.getX/setX` extensions; one-line delegates, no logic.
- `src/main/kotlin/Parts.kt` — `UShort.hibyte` / `UShort.lobyte`.
- `src/main/java/module-info.java` — the JPMS descriptor; the only Java source.
- `src/test/kotlin/TestUtils.kt` — `ByteArray.toHex()`, `Byte.toHex()`, `String.fromHex()`; tests express bytes as hex, so reuse these.

## Conventions

- **`kotlin { explicitApi() }` is on.** Every public declaration needs an explicit `public`, a
  return type and — by project convention — a KDoc block; that KDoc is what Dokka publishes.
- **Default endianness is `Endian.Big` everywhere**, on every accessor that takes one. See
  `R_endian_defaults_big`.
- **Files that `inline` trivial functions carry `@file:Suppress("NOTHING_TO_INLINE")`.**
- **A wider-typed setter overload silently ignores the high bits** — `setShort(Int)`,
  `setUShort(UInt)`. Documented behaviour, not a bug.
- **JVM target is 17** for both Kotlin and Java; raising it means updating the CI matrix too.
- **Tests are JUnit 5 with `kotlin.test.expect`**, grouped one `@Nested inner class` per operation
  and one `@Test` per value — so filter with the `Outer$Inner` form.
- **Hex literals ≥ 2^63 can't be written as `ULong`** (KT-4749); tests use `"deadbeef…".toULong(16)`
  in a top-level `private val` instead.
- There is no linter or formatter configured.

## Commands

- `./gradlew` — `defaultTasks = clean build`: compiles, runs tests, builds the jars.
- `./gradlew test` — tests only. Failed-test stack traces already print to stdout
  (`exceptionFormat = FULL`); no need for `--info` / `--stacktrace`.
- `./gradlew test --tests 'com.github.mvysny.unsigned.EndianTest$Little*'` — one nested class;
  `…PartsTest$UShort.hibyte` — one method.
- `./gradlew dokkaGeneratePublicationJavadoc` — API docs into `build/dokka/javadoc/`.
- `design/verify_design_tripwires.sh && design/verify_project_tripwires.sh` — the doc-layer and
  project tripwires.
- CI (`.github/workflows/gradle.yml`) runs `./gradlew clean build` on JDK 17/21/24 × Linux/macOS/
  Windows, plus the tripwires once on Linux.
- Releasing: see `CONTRIBUTING.md`.

## Skills this project follows

- **KDoc carries the per-symbol what *and* why, complete standalone**, and states the level each
  fact belongs at; the `writing-kdoc` skill has the rules.

## Working on this codebase

- **Don't re-enable the `javadoc` task.** It has nothing to read but `module-info.java` and
  rejects it; an empty `-javadoc.jar` fails nothing and shipped unnoticed for every release before
  0.4. If the jar comes up empty, look at `dokkaGeneratePublicationJavadoc`. See
  `R_javadoc_jar_has_docs`, `D_dokka_javadoc`.
- **`bin/`, `.classpath`, `.project`, `.settings/`** are Eclipse/Buildship output and **`build/`,
  `.gradle/`** are Gradle output — all git-ignored, none of them sources. Ignore them when searching.
