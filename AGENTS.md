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
| `design/requirements.md` | the promises the README's pitch makes — `R_` entries, stated not argued, **owner-written** | lazy |
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
- **A `D_` is earned by what happened, not by having had an alternative:** it shaped what the
  library is (reverse it and the README's first paragraph changes — the platform, the scope, the
  accessor set), or it cost research the next person would otherwise redo, and *Rejected:* says
  what was *done* to rule the road out. A testing library, the CI host, a version bump, a build
  plugin: a comment at the site of the choice, never an entry. Only decisions already taken; ideas,
  TODOs and open questions go to `design/ideas/`. A shipped decision that is reversed keeps its
  entry as a tombstone. Nothing about `design/` itself or its tooling is an entry.
- **An `R_` is a promise the README's pitch makes, made an official rule — and the owner writes
  it.** An agent never adds, edits or retires one; it proposes, in conversation or as a drafted
  entry in `design/ideas/`. The owner's ruler: allow the opposite everywhere — is it still the
  pitched library? "A NaN keeps its payload", "`endian` always defaults to `Endian.Big`" → not the
  same library → `R_`. "The `VarHandle`s stay top-level", "the three spellings of the module
  package match" → a build or a benchmark breaks, the library is the same → an *invariant*: one
  line below, named in the promise's *Enforced by*, no `R_`. "JVM target 17", "hex literals go
  through `toULong(16)`" → one convention line or one KDoc block.
- **An invariant is one line under *Invariants*, and nothing more:** the rule, at most one clause
  of consequence, `T_<slug>` if tripwired, `See D_<slug>` only when a `D_` exists. A line that will
  not fit belongs in the symbol's KDoc or in its `D_`.
- **Slugs:** `D_` decisions, `R_` requirements, `T_` tripwires (cited from a requirement's
  *Enforced by* or an invariant line here, defined by the check), `Q_` open questions inside
  `design/ideas/` only — a durable
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

- the choice made + the alternatives rejected → a `D_` entry in `design/decisions.md` if it passes
  the gate above; otherwise a comment at the site of the choice
- a promise the pitch makes that must hold from now on → a proposal for the owner, who writes the
  `R_` entry in `design/requirements.md`; the invariant that keeps one → a line below
- a new file, or a changed responsibility → one line in the module map below
- how the pieces work together — the delegation chain, a flow crossing several files → `design/architecture.md`
- what a competing library or built-in does → `design/comparison.md`, on its `A_` axes
- what one function does, its contract, its edge cases → its KDoc; `explicitApi()` requires one anyway
- usage, motivation, why you'd want this library → `README.md`
- the release process → `CONTRIBUTING.md`
- a cross-cutting invariant ("never …") → one line under *Invariants*: the rule, `T_<slug>` if
  tripwired, `See D_<slug>` if a `D_` exists
- work deferred *as a consequence of a logged decision* → that entry's *Consequences*

*Layout seeded from the `design-docs` and `agents-md` skills (mvysny, `~/.claude/skills`); this
project needs nothing from them.*

## Invariants

- **The six byte-array-view `VarHandle`s stay top-level `private val`s in `Endian.kt`** — inside the
  enum they compile, pass every test and are slower than the shifts they replaced.
  `T_varhandle_top_level`. See `D_varhandle`.
- **`setFloat`/`setDouble` write through `toRawBits()`, never `toBits()`** — and assert NaNs via
  `.toRawBits()`, since `kotlin.test.expect` canonicalizes too. `T_float_raw_bits`. See
  `R_no_nan_canonicalization`, `D_float_raw_bits`.
- **The JPMS module name, its `exports` and the `--patch-module` argument all name
  `com.github.mvysny.unsigned`** — when they drift javac says "package is empty or does not exist",
  and the tempting fix is an empty `Dummy.java`. `T_module_package_sync`. See `D_patch_module`.
- **The `-javadoc.jar` is filled from `dokkaGeneratePublicationHtml`; the `javadoc` task stays
  disabled** — it has nothing to read but `module-info.java`, and Dokka's *javadoc* renderer drops
  all 24 `@throws` tags. Either way the jar ships broken and nothing fails.
  `T_javadoc_jar_from_dokka`. See `D_dokka_html`.

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
- `./gradlew dokkaGeneratePublicationHtml` — API docs into `build/dokka/html/`; this fills the published jar.
- `design/verify_design_tripwires.sh && design/verify_project_tripwires.sh` — the doc-layer and
  project tripwires.
- CI (`.github/workflows/gradle.yml`) runs `./gradlew clean build` on JDK 17/21/24 × Linux/macOS/
  Windows, plus the tripwires once on Linux.
- Releasing: see `CONTRIBUTING.md`.

## Skills this project follows

- **KDoc states each fact at the level it belongs**: a contract shared by every accessor lives once
  on the `Endian` class doc, a per-symbol fact on the symbol. Members are deliberately *not*
  standalone. The `writing-kdoc` skill has the rules; `D_kdoc_voice` has what this project decided
  on top of them.

## Working on this codebase

- **`bin/`, `.classpath`, `.project`, `.settings/`** are Eclipse/Buildship output and **`build/`,
  `.gradle/`** are Gradle output — all git-ignored, none of them sources. Ignore them when searching.
