# Requirements

What must hold — of the library's behaviour and of the artifacts it publishes, not of the
environment it runs in. One entry per requirement. A requirement *states*; it never argues: the
fork behind it, if there is one, is a `D_` entry it cites.

- Cite by slug — `R_<slug>`. `grep '^## R_' design/requirements.md` is the index.
- Slug only what is referenced from elsewhere.
- Shape: `## R_<slug> — <the requirement, one sentence>`, then **Status** (Active, or Retired
  <date> — see `D_<slug>`), **Why** (one paragraph), **Enforced by** (a test, a compiler setting,
  a tripwire cited as `T_<slug>` — this line is that slug's home — or "review only"), **See**
  (the `D_` entries behind it).
- A retired requirement stays as a tombstone. A requirement that wants a *Rejected:* section is a
  decision — move it to `decisions.md`.
- **The first entry is the ruler**: later entries are trimmed to its length, never the other way
  round.

---

## R_varhandles_top_level — The six byte-array-view `VarHandle`s are top-level `private val`s in `Endian.kt`

**Status:** Active.
**Why.** C2 folds a `VarHandle` access into a single machine instruction only while the handle is
a `static final` field, which in Kotlin means a top-level `val`. Moving them into `Endian` as
constructor-injected instance fields is the obvious tidy-up and is the trap: it compiles, passes
every test, and silently degrades every accessor to a generic invocation *slower* than the
hand-rolled shifts the handles replaced. The damage is invisible without reading bytecode.
**Enforced by.** `T_varhandle_top_level`; confirm by hand with `javap -c` — the calls must read
`invokevirtual VarHandle.get:([BI)I`, never `:([BI)Ljava/lang/Object;`.
**See.** `D_varhandle`.

## R_module_package_sync — The JPMS module name, its `exports`, and the `--patch-module` target all name `com.github.mvysny.unsigned`

**Status:** Active.
**Why.** `module-info.java` is the only Java source; javac compiles it without seeing the Kotlin
output as part of the module, so `build.gradle.kts` patches that output in by package name. If the
three drift apart the build fails with "package is empty or does not exist", and the tempting fix
is to re-add an empty `Dummy.java` rather than to repair the patch.
**Enforced by.** `T_module_package_sync`.
**See.** `D_patch_module`.

## R_endian_defaults_big — Every `ByteArray` accessor that takes an `endian` parameter defaults it to `Endian.Big`

**Status:** Active.
**Why.** The default is the API's one piece of global consistency: a caller who omits `endian`
anywhere must get the same byte order everywhere, and a single overload defaulting to `Little`
would be a silent data-corruption bug at every call site that trusted the pattern. Byte-sized
accessors take no `endian` at all, since endianness is meaningless for one byte.
**Enforced by.** `T_endian_default_big`; `ByteArrayTest` asserts the default on each width.

## R_no_nan_canonicalization — The float accessors write the exact bits they are given: `toRawBits`, never `toBits`

**Status:** Active.
**Why.** `toBits()` rewrites every NaN to the canonical `0x7fc00000` / `0x7ff8000000000000`, and
every binary format that carries a float specifies a bit pattern — so canonicalizing on write is
non-conformance, not a cosmetic difference, and some protocols use NaN payloads as sentinels. The
swap compiles, and almost no test catches it: `kotlin.test.expect` boxes and `java.lang.Float.equals`
canonicalizes too, so an assertion written the obvious way passes either way. The promise is
one-directional — nothing here canonicalizes; a payload surviving a *round trip* is the platform's
business.
**Enforced by.** `T_float_raw_bits`.
**See.** `D_float_raw_bits`.

## R_javadoc_jar_has_docs — The published `-javadoc.jar` contains the rendered KDoc, not just a manifest

**Status:** Active.
**Why.** Maven Central requires a `-javadoc.jar` but never looks inside, so an empty one fails
nothing — every release up to 0.4 shipped one and nobody noticed. The `javadoc` task cannot fill it
(it reads Java sources, and there are none but `module-info.java`), so the jar is fed from Dokka's
HTML renderer and `javadoc` stays disabled.
**Enforced by.** `T_javadoc_jar_from_dokka`.
**See.** `D_dokka_html`.

## R_bounds_contract_published — The out-of-range contract reaches the published javadoc jar, not only the `@throws` tags

**Status:** Retired 2026-09-12 — see `D_dokka_html`.
**Why.** It pinned the `Endian` class doc's prose bullet while that bullet was the only statement of
the contract reaching the jar — Dokka's **javadoc** renderer dropped the 24 `@throws` tags silently.
`D_dokka_html` fills the jar from the HTML renderer, which renders them, so the tags now meet this
on their own. The bullet stays; nothing pins it.
**Enforced by.** Nothing — its tripwire was deleted along with the requirement.
**See.** `D_dokka_html`, `D_kdoc_voice`.
