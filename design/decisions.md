# Decisions

Why this library is the way it is: one entry per decision *already taken*, with the roads not
taken. Not what the code does (`src/` and its KDoc), not what must hold (`requirements.md`), not
what the alternatives do (`comparison.md`).

- Cite an entry by slug — `D_<slug>` — never by position. `grep '^## D_' design/decisions.md`
  is the index; there is no table of contents.
- **An entry is earned by what happened, not by having had an alternative:** the decision shaped
  what the library is (reverse it and the README's first paragraph changes — the platform, the
  scope, the accessor set), or it cost research the next person would otherwise redo, and
  *Rejected:* then says what was *done* to rule the road out — measured, tried, read. The testing
  library, the CI host, a version bump, a build plugin: two sentences in a comment at the site of
  the choice, never an entry. Nothing about `design/` itself or its tooling is an entry.
- Entries are mutable: refine in place, newest first. A *shipped* decision that is reversed
  keeps its entry as a tombstone (`Status: Superseded by D_<slug>`); the replacement is written fresh.
- Shape: `## D_<slug> — <title> (<decided date>)`, then **Status**, the context and the decision,
  one **Rejected: …** paragraph per alternative, and the consequences.
- **The oldest entry — at the bottom — is the ruler**: later entries are trimmed to its length,
  never the other way round.

---

## D_dokka_html — Dokka's HTML fills the javadoc jar; the `javadoc` task is disabled (2026-09-12)

**Status:** Shipped 2026-09-12.

Maven Central requires a `-javadoc.jar`. The stock `javadoc` task cannot produce one here: it reads
Java sources, and this library has none but `module-info.java` — which it rejects with the same
"package is empty" error as `D_patch_module`. That error was once muted with `isFailOnError = false`,
so the build stayed green while every release up to 0.4 shipped a `-javadoc.jar` containing nothing
but a manifest. Nobody noticed, because nothing fails when API docs are missing — it just makes the
library worse to use. So `build.gradle.kts` applies `org.jetbrains.dokka`, disables `javadoc`
outright, and fills the jar from `dokkaGeneratePublicationHtml`. `withJavadocJar()` is deliberately
kept: it registers the `javadocElements` variant, so the Gradle module metadata still advertises the
jar.

**Rejected: Dokka's `javadoc` output format** (`org.jetbrains.dokka-javadoc`), which this project
shipped first and used until this entry reversed it. Both formats satisfy Central, which only checks
that a signed `-javadoc.jar` exists and never looks inside; the jar keeps its name and classifier
either way. The javadoc format was picked for the layout other tooling expects — `element-list`,
`package-summary.html`, `index-files/`, `member-search-index.js` — so that javadoc `-link` and IDE
"external documentation" resolve against it. Rendering both and diffing them showed what that costs:

| | `dokkaGeneratePublicationJavadoc` | `dokkaGeneratePublicationHtml` |
|---|---|---|
| the 24 `@throws` tags | **dropped — 0 occurrences in the output** | a Throws section on every accessor page |
| `element-list`, `index-files/`, `member-search-index.js`, `package-summary.html` | yes | no |
| `package-list` | at the jar root | under `kotlin-unsigned-jvm/` |

`@param` and `@return` survive both, so the tag is the only casualty — the fault is in the renderer,
which JetBrains ship separately and label Alpha
([Kotlin/dokka#4602](https://github.com/Kotlin/dokka/issues/4602), and #2262 for the shape of it).

The layout it buys is worth nothing *here*. It serves Java tooling, and this library is barely
callable from Java at all: every unsigned accessor returns a value class, so the JVM names are
mangled. Kotlin consumers read the published **sources jar** in the IDE; javadoc.io serves either
tree; nothing links to us with `-link`. What it costs is the bounds contract on 24 of 26 public
functions.

**Don't re-enable `javadoc`.** It has nothing to read. If the jar ever comes up empty again, the
task to look at is `dokkaGeneratePublicationHtml`.

**Consequences.** The requirement that pinned the out-of-range prose, and its tripwire, are retired:
the tags now reach the jar on their own, so nothing pins that prose bullet — which stays,
because a contract shared by every accessor belongs on the class doc regardless. Anyone wanting
javadoc `-link` against this library is worse off, per the table; accepted.

## D_kdoc_voice — KDoc is written for this API, not transcribed from Dart; contracts go in prose (2026-09-12)

**Status:** Shipped 2026-09-12.

Every accessor carried Dart's `ByteData` documentation near-verbatim: three paragraphs apiece — what
it returns, the range of the value, the offset precondition — in about twenty copies, with
`ByteArrays.kt` a second copy of `Endian.kt`'s. Beyond the repetition, three things were actually
wrong:

* **The range paragraphs documented a deficiency this library does not have.** "The return value
  will be between 0 and 2^16 - 1, inclusive" is load-bearing in Dart, where `ByteData.getUint16`
  returns `int` because Dart has no unsigned type. Here `getUShort` returns `UShort` and the
  sentence restates the signature. We had imported Dart's compensation for the exact gap this
  library exists to close, into the one slot reserved for what the type *cannot* say.
* **"this object" pointed at the wrong object.** Dart's methods are on the receiver; `Endian`'s take
  the array as a parameter, so on every member of `Endian` the phrase named the enum constant while
  meaning `bytes`.
* **Three setters claimed a precondition they do not enforce.** `setByte(Int)`, `setUByte(UInt)` and
  `setUShort(UInt)` all said the value "must fit"; all three truncate in silence, as `README.md` and
  `AGENTS.md` had said all along. Only `setShort(Int)` documented it.

Rewritten to the `writing-kdoc` skill: one summary sentence per member, the cross-cutting contracts
(unaligned offsets, out-of-range throwing and its width-counted message, silent truncation in the
wider-typed setters) stated once on the `Endian` class doc, and per-member blocks carrying only what
is specific to them.

**Rejected: keeping the Dart phrasing for portability.** It was a deliberate convention, on the
reasoning that someone porting Dart code would recognize the wording. Mimicking `ByteData`'s *API*
is the library's whole pitch; mimicking its *prose* imports the limits of Dart's type system into
documentation for a language that does not share them. The API names still match, which is what a
porter actually greps for.

### The bounds contract: `@throws` tags, not prose

`ByteArrays.kt` documents the bounds contract with `@throws` — a documented KDoc block tag, listed
in [Kotlin's KDoc reference](https://kotlinlang.org/docs/kotlin-doc.html), which Dokka parses
correctly. It was contested only because the renderer then filling the published jar dropped it
silently; `D_dokka_html` has the measurement, and swapped the renderer.

**Rejected: dropping the tag and writing the clause in prose on each function.** While that renderer
was in use, prose was the only form that reached every channel, which looked strictly better. But
banning a standard language tag permanently — with a tripwire, no less — to route around one broken
renderer is the wrong component to change, and the **sources jar** (`withSourcesJar()`), which is
the primary documentation channel for a Kotlin consumer's IDE, rendered the tag correctly all along.

**Consequences.** `Endian`'s members are deliberately no longer standalone — they lean on the class
doc for the shared contract, which is the skill's rule and a reversal of the "complete standalone"
line `AGENTS.md` used to carry. The asymmetry between the two files is intentional: `Endian` has a
class doc to hoist contracts onto, and `ByteArrays.kt` — top-level extensions — has no such slot, so
each function restates the bounds clause. A Dokka `package.md` was considered as that missing slot
and rejected: it is a separate file that nothing load-bearing may live only in, so it would not have
saved the restatement.

## D_floats_in_scope — `Float`/`Double` accessors belong here, "unsigned" in the name notwithstanding (2026-09-12)

**Status:** Shipped 2026-09-12.

`Float` has no signed/unsigned dimension at all, so "why does an *unsigned* library have floats?" is
the objection someone re-proposes every couple of years. Recorded once, here.

The scope was never "unsigned only", and floats are not what broke it: `getShort`, `getInt` and
`getLong` are signed and have been from the start. `unsigned` in the name describes **what was
missing from the alternatives** — `DataInputStream` and `ByteBuffer` widen instead of returning
unsigned types (see the `A_kotlin_unsigned` column in [comparison.md](comparison.md)) — not the
boundary of what this library does. What the library actually is, and what `README.md` has claimed
since its first line, is *Dart's `ByteData` for Kotlin*: random access into a plain `ByteArray` at a
byte offset, with endianness an ordinary argument. Floats are a `ByteData` accessor and fit that
sentence exactly.

The cost is eight functions and no new logic — `getFloat` is `Float.fromBits(getInt(…))` and
`setFloat` is `setInt(…, value.toRawBits())`, so `Endian`'s two constants are untouched and
`EndianTest`'s existing `getInt`/`getLong` coverage carries the correctness weight. Endianness
applies to the *container*, not to the number: IEEE 754 fixes what a 32-bit pattern means and says
nothing about which order the bytes hit the wire, and no format anywhere reverses the mantissa bytes
but not the exponent bytes. Whole-word reversal is the entirety of it, which is why delegating to
`getInt`/`getLong` is not merely convenient but exactly right.

**Rejected: leaving the gap open.** Honest framing first — this was *positioning*, not demand.
Nobody asked; the library's origin (a Renogy Rover) used no floats at all, because sensor protocols
carry quantities as scaled integers (`D_no_unit_scaling`). The multiplatform idea had exactly the
same character and was declined for it (`D_jvm_only`). What decides it differently here is cost:
`D_jvm_only` bought a permanent second implementation, a rename and a wider CI matrix, while this
buys eight delegating functions. And the gap was load-bearing *against* the library in its own docs —
`comparison.md` named it three times and `README.md` had to concede it mid-argument — so keeping it
was a recurring tax paid to avoid a one-off cost.

**Rejected: `getFloat32`/`getFloat64`, Dart's names.** The API is named after the Kotlin type it
returns (`getInt`, not `getInt32`); matching ourselves beats matching Dart, and `ByteBuffer` agrees.
The same rule is what keeps half-precision out (`D_no_float16`).

**Rejected: a `setFloat(Double)` widening overload.** Kotlin has no implicit numeric widening, so
`bytes.setFloat(0, 1.0)` is *already* a compile error; adding the overload would manufacture a silent
precision loss where the language had prevented one. This is a point of superiority over Dart, whose
`ByteData.setFloat32` must take a `double` and round, since Dart has no `float` type. Note the
contrast with the existing integer setters, where a wider overload *is* offered and documented to
drop the high bits — truncating an `Int` to 16 bits is exact and reversible in a way rounding a
`Double` to binary32 is not.

**Consequences.** The remaining `ByteData` features this library lacks are the typed-list views
(`Float32List` and friends), which are a different concept — a view object over a buffer — and out of
scope under `A_no_wrapper`. `README.md` can therefore claim `ByteData` scalar parity as fact.

## D_float_raw_bits — the float accessors never canonicalize a NaN (2026-09-12)

**Status:** Shipped 2026-09-12.

`setFloat` goes through `Float.toRawBits()`, never `Float.toBits()`. The two differ on exactly one
input class: `toBits()` rewrites every NaN to the canonical `0x7fc00000`, `toRawBits()` preserves the
pattern it was given. Every binary format that carries a float specifies a *bit pattern* — CBOR,
MessagePack, Thrift, Protobuf, Avro, BSON, WAV, Parquet, HDF5, numpy all `memcpy` the 4 or 8 bytes —
and some protocols use NaN payload bits as sentinels, so a library that silently rewrote one on write
would be non-conforming, not merely surprising.

**The promise is one-directional, and the wording matters.** What is guaranteed is that *this library
never canonicalizes*: `setFloat` writes exactly the bits of the `Float` it is handed. What is **not**
promised is that a NaN payload survives a round trip through the byte array — the read side
materializes a `Float` via `Float.fromBits`, which compiles to `java.lang.Float.intBitsToFloat`,
whose javadoc explicitly declines it: *"this method may not be able to return a float NaN with
exactly the same bit pattern as the int argument."* That lossy step is the platform's, not ours.

The distinction is not pedantry: it costs nothing here and it is the wording that would survive a
port. Even though the library is JVM-only (`D_jvm_only`), a Kotlin/JS `Float` is a double at runtime
and engines canonicalize NaN aggressively, so the stronger promise would be false there on day one.

**Rejected: pinning a NaN-payload round trip as a correctness test.** A payload NaN can only be
*produced* by `Float.fromBits`, so any such assertion tests `fromBits(x).toRawBits() == x` — the exact
composition the javadoc carves out — on a 3-OS × 3-JDK matrix. The library's own contract is enforced
by the absence of `toBits` from the source (`R_no_nan_canonicalization`, `T_float_raw_bits`), not by a
test. `EndianTest.NaNPayloadIsAPlatformProperty` keeps the vectors anyway, labelled as a platform
probe: a JDK that mangles a *quiet* NaN payload is worth finding out about, and the comment tells
whoever sees it go red to delete the probe rather than "fix" the accessors.

**Rejected: `kotlin.test.expect(Float.NaN) { … }` for any NaN assertion.** `expect` resolves to the
generic `assertEquals`, which boxes and calls `java.lang.Float.equals` — which compares
`floatToIntBits` and therefore canonicalizes. `expect(Float.fromBits(0x7fc0dead)) { … }` passes even
when the payload was mangled. Every NaN assertion in the suite goes through `.toRawBits()`, and the
constants carry a comment saying why, because this is precisely the line a later tidy-up "simplifies".
The mirror case is free: boxed equality *does* distinguish `0.0f` from `-0.0f`, so the sign-bit
vectors read naturally.

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
at all — see `D_dokka_html`.

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
