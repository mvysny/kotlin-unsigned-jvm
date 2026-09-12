# Design decisions

Why the API looks the way it does, and which designs were tried and rejected — the things that are
invisible in the code and silent under test, so a future reader would otherwise re-propose them.

Not a changelog, and not a home for anything the code already says. Add an entry only when the
reasoning would be genuinely hard to reconstruct from `src/` and the KDoc.

Each entry gets a stable `D_`-slugged heading so it can be referenced from elsewhere (a KDoc line, a
commit message, an `ideas/` file) without a bare number that rots on reordering.

Decisions about *other* libraries and built-in APIs — why not `ByteBuffer`, why not kotlinx-io — live
in [COMPARISON.md](COMPARISON.md) instead, since that's where a reader comparing options will look.

---

## `D_patch_module` — JPMS: patch the Kotlin output into the module, don't fake the package

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

The two produce an identical jar: same 13 entries, and `jar --describe-module` reports
`com.github.mvysny.unsigned@<version>` exporting the package and requiring `kotlin.stdlib` either way.

**Not fixed by this:** the `javadoc` task still hits the same "package is empty" error, because it doesn't get
the `--patch-module` argument. It is muted by `isFailOnError = false` in `build.gradle.kts`, and the published
`-javadoc.jar` has therefore always been empty (manifest only). Giving javadoc a real Kotlin-aware doc tool
(Dokka) is the fix worth making, not a second `--patch-module`.
