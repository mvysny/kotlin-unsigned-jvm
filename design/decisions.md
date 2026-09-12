# Decisions

Why this library is the way it is: one entry per decision *already taken*, with the roads not
taken. Not what the code does (`src/` and its KDoc), not what must hold (`requirements.md`), not
what the alternatives do (`comparison.md`).

- Cite an entry by slug — `D_<slug>` — never by position. `grep '^## D_' design/decisions.md`
  is the index; there is no table of contents.
- **No entry without a real fork.** Nothing seriously considered and rejected → not a decision.
- Entries are mutable: refine in place, newest first. A *shipped* decision that is reversed
  keeps its entry as a tombstone (`Status: Superseded by D_<slug>`); the replacement is written fresh.
- Shape: `## D_<slug> — <title> (<decided date>)`, then **Status**, the context and the decision,
  one **Rejected: …** paragraph per alternative, and the consequences.

---

## D_no_unit_scaling — the API stops at the bit pattern; scale, offset and units are the caller's (2026-09-12)

**Status:** Accepted 2026-09-12.

Surveying how binary formats carry a physical quantity turns up two industries answering two
different questions. *"How do I serialize a float"* is answered overwhelmingly by IEEE-754 — the raw
binary32 / binary64 pattern, byte order the only free variable — in CBOR, MessagePack, Thrift,
Protobuf, Avro, BSON, WAV float PCM, Parquet and numpy alike. *"How do I serialize a voltage"* is
answered just as overwhelmingly by **scaled integers**: Renogy's ÷100, CAN / OBD-II's per-PID
scale-and-offset, most of IEC 61850, most sensor BLE. Embedded designers avoid floats because the MCU
may have no FPU, and because `2560` is exact where `25.6f` is not. This library's own origin — driving
a Renogy Rover over Modbus RTU — used no floats at all.

So the library's accessors return and accept **the bit pattern the wire carries**, nothing more. A
caller who wants volts writes `bytes.getUShort(4, Endian.Little).toInt() / 100.0`, and that division
is the entirety of what a scaling API would do for them.

**Rejected: a scaled accessor** — `getScaled(byteOffset, scale, offset)`, or a `Scale` parameter
alongside `endian`. To be worth more than the division it replaces it would have to carry the
metadata that makes scaling meaningful: which field uses which scale, the zero offset, the unit,
saturation behaviour at the type's edge, and whether the raw field is signed. That is a description
of one device's register map; it belongs in the driver that owns the map. The division is already the
most readable line in such a driver, and it is the one place the unsigned types earn their keep —
`0xFFFF / 100.0` is `655.35` through `UShort` and `-0.01` through `Short`.

**Rejected: reading this as an argument against IEEE-754 accessors.** The finding says scaled
integers are what sensor protocols actually use, so floats are not where this library's *demand*
lies. It does not say byte-level float accessors are wrong to have — a format that does use IEEE-754
still needs its four or eight bytes turned into a number, which is exactly this library's job.

**Consequences.** "Why doesn't this have `getScaled`?" resolves here. A protocol-specific helper
belongs in a protocol-specific library that depends on this one.

## D_no_float16 — no half-precision accessors: not binary16, not bfloat16 (2026-09-12)

**Status:** Accepted 2026-09-12; the JDK-baseline argument below expires on its own, the other two
do not.

"Float16" is **IEEE-754 binary16** (standardised 2008) — the same anatomy as `Float` with smaller
fields, and therefore the same exponent-bias scheme, subnormals, infinities and NaN encodings:

| | sign | exponent | mantissa | bias | max finite | smallest normal | ~decimal digits |
|---|---|---|---|---|---|---|---|
| binary16 (half) | 1 | 5 | 10 | 15 | 65504 | 6.10 × 10⁻⁵ | ~3.3 |
| binary32 (`Float`) | 1 | 8 | 23 | 127 | 3.40 × 10³⁸ | 1.18 × 10⁻³⁸ | ~7.2 |
| binary64 (`Double`) | 1 | 11 | 52 | 1023 | 1.80 × 10³⁰⁸ | 2.23 × 10⁻³⁰⁸ | ~15.9 |
| bfloat16 | 1 | 8 | 7 | 127 | 3.39 × 10³⁸ | 1.18 × 10⁻³⁸ | ~2.4 |

Practically: ~3 significant decimal digits, overflowing to infinity above 65504 — fine for a
temperature or a normalised colour, useless for a distance in millimetres. You meet it in CBOR (major
type 7, additional info 25), OpenEXR, GPU texture and vertex data, ML model files, occasionally a
sensor payload.

**Rejected: `getFloat16` / `setFloat16`.** Three reasons, and only one of them ages:

* **No Kotlin type to name it after.** It would have to return `Float`, and the whole API is named
  after the Kotlin type it hands back — `getInt`, not `getInt32`. A `getFloat16` returning a `Float`
  is the first accessor whose name describes the wire rather than the return type.
* **No JDK help at this baseline.** JDK 20 added `Float.float16ToFloat(short)` /
  `floatToFloat16(float)`; this library targets 17 (and CI runs 17/21/24), so it would be ~20
  hand-rolled lines whose subnormal and overflow edges would be the only real correctness risk in the
  whole float story — everything else here is delegation to code the JDK already tests.
* **The name is ambiguous in this library's own domain.** Bluetooth LE's health and battery profiles
  define `SFLOAT`: also 16 bits, but a 4-bit *decimal* exponent plus a 12-bit mantissa — scaled-integer
  thinking (`D_no_unit_scaling`), not IEEE. Someone doing sensor work who sees `getFloat16` may well
  expect that one.

**Rejected: bfloat16.** The confusable sibling — binary32 with the low 16 mantissa bits chopped off,
keeping the *range* and discarding the *precision*, so conversion to and from `Float` is a shift
rather than an algorithm. It exists for machine learning and is met nowhere else; none of the formats
this library targets carry it.

**Consequences.** If this is ever revived — CBOR is the likeliest trigger — it gets its own idea file,
and the name to reach for is `getHalf` / `setHalf`: it sidesteps the `SFLOAT` collision and reads as a
width rather than as a claim about the return type.

## D_design_docs — Adopt the `design/` doc layer, with `architecture.md` as the assembled picture (2026-09-12)

**Status:** Accepted; installed 2026-09-12.

Before this, the prose lived in shouting UPPERCASE files at the root — `DECISIONS.md`,
`COMPARISON.md`, a root `ideas/` — and `AGENTS.md` carried a "Layout and how the pieces fit"
section that was really architecture prose plus a graduation table, paid for on every turn of every
session. Nothing stated *what must hold*: the two invariants that are genuinely easy to break from a
distance (the top-level `VarHandle`s, the three-way JPMS package name) lived only as paragraphs
inside `D_varhandle` and `D_patch_module`, where an agent editing `build.gradle.kts` would not look.

Rationale and reference move under `design/` — `decisions.md` (`D_`), `requirements.md` (`R_`),
`architecture.md`, `comparison.md`, `ideas/` — and `AGENTS.md` keeps only invariants, the module
map and the doc map, under the 34 KB cap its header states.

The assembled picture is **`architecture.md`, a description**: `kotlin { explicitApi() }` forces a
KDoc block on every public declaration and Dokka publishes those blocks as the `-javadoc.jar`
(`D_dokka_javadoc`), so per-symbol truth is complete in the source. When `architecture.md` and the
code disagree, the file is what gets fixed.

**Rejected: keeping the rationale in `AGENTS.md`.** It is loaded on every turn; the `D_varhandle`
benchmark table alone is a third of the file's budget, and compressing it into a bullet would make
a second copy that drifts from the entry it points at.

**Rejected: `solution.md`.** That is for a project whose sources are written *against* a spec — an
install script, thin glue over an upstream product — where a mismatch means the code is wrong. Here
there are three Kotlin files of ~300 lines whose KDoc is the published API documentation; a spec
would be a second description of them, and the second copy would lose.

**Consequences.** Every `D_` / `R_` / `T_` slug cited anywhere must resolve —
`design/verify_design_tripwires.sh` and `design/verify_project_tripwires.sh` check it, and CI runs
both.

## D_patch_module — JPMS: patch the Kotlin output into the module, don't fake the package (2026-09-12)

**Status:** Shipped 2026-09-12 (`ebf97f6`).

`src/main/java/module-info.java` exports `com.github.mvysny.unsigned`, a package implemented entirely in
Kotlin. javac compiles the module descriptor on its own and does not consider the Kotlin output part of the
module being compiled, so the export fails to resolve:

```
module-info.java:4: error: package is empty or does not exist: com.github.mvysny.unsigned
```

`build.gradle.kts` fixes this by patching the Kotlin classes into the module for `compileJava`:

```kotlin
options.compilerArgumentProviders.add(CommandLineArgumentProvider {
    listOf("--patch-module", "com.github.mvysny.unsigned=" + mainClassesOutput.asPath)
})
```

**Rejected: an empty `Dummy.java` in the exported package.** This was the original fix (removed 2026-09-12),
carried as a workaround for
[KT-55389](https://youtrack.jetbrains.com/issue/KT-55389/Gradle-plugin-should-expose-an-extension-method-to-automatically-enable-JPMS-for-Kotlin-compiled-output):
a file holding nothing but `package com.github.mvysny.unsigned;`, enough for javac to consider the package
non-empty. It worked, but it lied — nothing patched the real classes in, javac just stopped asking. The costs
were that it shipped in the sources jar as a puzzling empty file, and that its own comment misdescribed the
mechanism (it claimed to exist so `compileJava` would run at all; `compileJava` runs regardless, since
`module-info.java` is itself a Java source). Don't re-add it. If `exports` breaks again, the `--patch-module`
path is what needs fixing.

The two produce an identical jar: same entry count, and `jar --describe-module` reports
`com.github.mvysny.unsigned@<version>` exporting the package and requiring `java.base` + `kotlin.stdlib`
either way. That count is 14 as of `D_varhandle`, which added an `EndianKt.class` to hold the
`VarHandle` fields — `Endian.kt` had no top-level declarations before, so no file class was emitted.

The `javadoc` task used to hit the same error for the same reason; that is now moot, since it no longer runs
at all — see `D_dokka_javadoc`.

## D_varhandle — `Endian` reads and writes through byte-array-view `VarHandle`s (2026-09-12)

**Status:** Shipped 2026-09-12 (`2b53cda`).

`Endian.kt` used to assemble every value by hand — `(bytes[o].toUByte().toUInt() shl 24) + …` and the
matching `ushr`/`toByte()` stores. Each constant now delegates to one of six
`MethodHandles.byteArrayViewVarHandle` handles (short/int/long × big/little), which C2 compiles to a
single unaligned, byte-swapping load or store.

Measured on JDK 25 (JBR), 4096-byte array, best-of-2000 reps after warmup, three agreeing rounds:

| | shift-or | `VarHandle` | |
|---|---|---|---|
| `getInt` | 0.77 ns | 0.25 ns | **3.1×** |
| `getLong` | 1.40 ns | 0.25 ns | **5.6×** |
| `setLong` | 0.23 ns | 0.25 ns | parity |
| `setLong`, `-XX:-MergeStores` | 0.45 ns | 0.25 ns | **1.8×** |

Stores look like parity only because C2's merge-stores optimization (JDK 23+) already collapsed the
hand-rolled version. The last row disables it to stand in for JDK 17/21, half the CI matrix, where the
`VarHandle` store wins too. Loads gain everywhere: there is no corresponding merge-*loads* pass.

**The handles must be top-level `private val`s.** C2 folds a `VarHandle` call into one instruction only
while it can see the handle as a constant, and that means a `static final` field. Passing them into the
enum's constructor as instance `val`s is the obvious tidy-up and is the trap: an enum is not a class C2
trusts final instance fields on, so every access silently degrades to a generic `VarHandle` invocation —
*slower* than the shifts this replaced. It compiles, and all tests pass. Verify with
`javap -c`: the calls must read `invokevirtual VarHandle.get:([BI)I`, never `:([BI)Ljava/lang/Object;`.

**Not a behaviour change.** Out-of-range offsets still throw `ArrayIndexOutOfBoundsException` — the view
handle throws the array subclass, not a bare `IndexOutOfBoundsException`. Only the *message* differs: it
counts in units of the value's width, so an `Int` read at offset 2 of a 4-byte array says "index 2 out of
bounds for length 1". That quirk is noted in the `Endian` KDoc.

**Rejected: leaving the arithmetic alone and only tidying it.** Dropping the redundant top-byte mask and
building in `Int`/`Long` rather than through `UInt`/`ULong` reads better, but the `UByte`/`UInt`
round-trips were already free (inline value classes compile to `and 0xFF`), so it buys nothing
measurable. The cleanup is worth having only if the `VarHandle`s are ever backed out.

The cost is that `Endian.kt` now names a JVM API, where before all three source files were pure Kotlin. That
demotes a hypothetical multiplatform port from a file move to a `jvmMain` actual over a shift-or `commonMain`
fallback, and is one of the three costs weighed in `D_jvm_only`.

## D_dokka_javadoc — Dokka fills the javadoc jar; the `javadoc` task is disabled (2026-09-12)

**Status:** Shipped 2026-09-12 (`6d611c1`).

Maven Central requires a `-javadoc.jar`. The stock `javadoc` task cannot produce one here: it reads Java
sources, and this library has none but `module-info.java` — which it rejected with the same "package is empty"
error as `D_patch_module`. That error was muted with `isFailOnError = false`, so the build stayed green while
every release from the start shipped a `-javadoc.jar` containing nothing but a manifest. Nobody noticed,
because nothing fails when API docs are missing — it just makes the library worse to use.

`build.gradle.kts` now applies `org.jetbrains.dokka` + `org.jetbrains.dokka-javadoc`, disables `javadoc`
outright, and fills the jar from `dokkaGeneratePublicationJavadoc`. `withJavadocJar()` is deliberately kept:
it registers the `javadocElements` variant, so the Gradle module metadata still advertises the jar.

**Why the `javadoc` *format*, not Dokka's default HTML.** Both would satisfy Central, which only checks that a
signed `-javadoc.jar` exists and never looks inside. The javadoc format is chosen because it keeps the layout
consumers' tooling expects — `element-list`, `package-list`, `package-summary.html`, `index-files/`,
`member-search-index.js` — so IDE "external documentation" links and `@link`-style cross-references from other
projects resolve. Dokka's HTML format is prettier but is not a drop-in for that.

**Don't re-enable `javadoc`.** It has nothing to read. If the jar ever comes up empty again, the task to look
at is `dokkaGeneratePublicationJavadoc`.

## D_jvm_only — stay JVM-only; multiplatform is deferred, not foreclosed (2026-09-12)

**Status:** Accepted 2026-09-12 (`99c78e2`); revisit only when a user asks for Native or JS.

The niche is real: nothing in Kotlin offers random access into a plain `ByteArray` returning true unsigned
types on every platform (kotlinx-io and Okio are cursors, korlibs widens to `Int`/`Long`, the Kotlin/Native
stdlib is experimental with undocumented endianness — see the `A_multiplatform` row in
[comparison.md](comparison.md)). It is also *empty*, and this library is the obvious thing to fill it with.
Rejected anyway, as of 2026-09-12: the JVM is the only platform this is shipped on, so the argument is
positioning, not need — and the costs are recurring while the benefit is speculative.

What it would actually cost:

* **Two implementations of the core, permanently.** `D_varhandle` is JVM-only, so `Endian` becomes the one
  `expect`/`actual` split — the `VarHandle` version as the `jvmMain` actual, the shift-or arithmetic (git
  still has it at `6d611c1`) as `commonMain`. That duplicates the one file where an off-by-one is easiest to
  write and hardest to spot. `ByteArrays.kt` and `Parts.kt` still move verbatim; they are pure Kotlin.
* **`module-info.java` inside a KMP `jvm()` target.** `D_patch_module`'s `--patch-module` path points at
  `sourceSets.main.output` and would have to follow the Kotlin output wherever the KMP plugin puts it, across
  plugin versions. This is the fiddliest part of the whole change.
* **The artifact name.** A KMP build appends the target name, so the JVM artifact would publish as
  `kotlin-unsigned-jvm-jvm`, and `-jvm` is baked into the group id too. Three ways out, none free: new
  coordinates (`com.github.mvysny.kotlin-unsigned:kotlin-unsigned`) with the old artifact frozen; keep the
  coordinates and override the JVM `artifactId` so existing build files still resolve, at the cost of a
  permanently confusing group id; or publish both and deprecate the old one over a release or two.

**Rejected reasoning: "the CI matrix has to grow to x86 / ARM / …".** True of something like the Native
stdlib's `getIntAt`, which is a raw memory reinterpret — that is exactly why its endianness is undocumented.
It is *not* true here. The `commonMain` implementation reads `array[i]` one byte at a time and shift-ors, so
host endianness never enters the expression and no multi-byte load ever happens: `linuxArm64` cannot disagree
with `linuxX64`. The axis that does carry real risk is **JS**, where `Long`/`ULong` are emulated and the
64-bit paths would be running for the first time. That is one extra runner, not a multiplied matrix. So CPU
architecture is not a reason to decline — the three costs above are.

**If this is ever revisited, the testing shape to use:** keep the shift-or path as `internal` *common* code
and let the JVM `actual` delegate to the `VarHandle`s, so the JVM suite can run the full battery against both
implementations. Native and JS CI then need only a smoke test, and the duplicate-core cost above drops
sharply.

Deferring is cheap because nothing here is a one-way door: the code is still migration-ready, and the day a
user actually asks for Native or JS, the rename has a motivation instead of being churn. The one cost that
does grow with time is the coordinates — each release adds users pinned to the `-jvm` name — but the
`artifactId` override above is a real escape hatch, and Maven Central relocation POMs exist. Not enough to
justify renaming today.
