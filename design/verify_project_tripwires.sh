#!/usr/bin/env bash
# This project's own mechanical invariants — the T_ checks cited from design/requirements.md.
# The generic doc-layer checks (slug resolution, AGENTS.md caps, the CLAUDE.md shim) are the
# other script: design/verify_design_tripwires.sh.
#
# Each check below guards a rule that compiles, passes every test, and is wrong anyway.
# A failure is as often a stale expectation as a real violation — read the rule before "fixing".
#
# Run from anywhere inside the repo: design/verify_project_tripwires.sh
# Exit 0 when green, 1 with one "tripwire:" line per failure otherwise.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

fail=0
err() { printf 'tripwire: %s\n' "$*" >&2; fail=1; }

ENDIAN=src/main/kotlin/Endian.kt
BYTEARRAYS=src/main/kotlin/ByteArrays.kt
MODULE_INFO=src/main/java/module-info.java
BUILD=build.gradle.kts

# --- T_varhandle_top_level ----------------------------------------------------
# The six byte-array-view VarHandles must be top-level `private val`s: C2 folds a VarHandle access
# into one instruction only while the handle is a static final field. Moving them into the enum as
# instance fields compiles, passes every test, and is slower than the shifts they replaced.
# See R_varhandles_top_level in design/requirements.md, D_varhandle.
n=$(grep -c '^private val [A-Z_]*: VarHandle =$' "$ENDIAN" || true)
[ "$n" -eq 6 ] || err "T_varhandle_top_level: $ENDIAN declares $n top-level \`private val …: VarHandle\`, expected 6 — they must NOT move into the enum (R_varhandles_top_level, D_varhandle)"

# --- T_module_package_sync ----------------------------------------------------
# The JPMS module name, its exports, and the --patch-module target must name the same package,
# or javac rejects module-info.java with "package is empty or does not exist".
# See R_module_package_sync in design/requirements.md, D_patch_module.
pkg_module=$(sed -n 's/^module \([a-z0-9._]*\) {.*/\1/p' "$MODULE_INFO")
pkg_exports=$(sed -n 's/^ *exports \([a-z0-9._]*\);.*/\1/p' "$MODULE_INFO")
pkg_patch=$(sed -n 's/.*"--patch-module", "\([a-z0-9._]*\)=.*/\1/p' "$BUILD")
for got in "$pkg_exports" "$pkg_patch"; do
  [ -n "$got" ] || err "T_module_package_sync: could not find the package name in $MODULE_INFO / $BUILD — did the syntax change? (R_module_package_sync)"
done
[ "$pkg_module" = "$pkg_exports" ] || err "T_module_package_sync: module is \`$pkg_module\` but exports \`$pkg_exports\` (R_module_package_sync, D_patch_module)"
[ "$pkg_module" = "$pkg_patch" ]   || err "T_module_package_sync: module is \`$pkg_module\` but --patch-module targets \`$pkg_patch\` (R_module_package_sync, D_patch_module)"

# --- T_endian_default_big -----------------------------------------------------
# Every endian-taking accessor defaults to Endian.Big; one overload defaulting to Little would
# silently corrupt data at every call site that trusted the pattern.
# See R_endian_defaults_big in design/requirements.md.
total=$(grep -c 'endian: Endian' "$BYTEARRAYS" || true)
defaulted=$(grep -c 'endian: Endian = Endian\.Big' "$BYTEARRAYS" || true)
[ "$total" -eq "$defaulted" ] || err "T_endian_default_big: $BYTEARRAYS has $total \`endian: Endian\` parameters but only $defaulted default to Endian.Big (R_endian_defaults_big)"

# --- T_float_raw_bits ---------------------------------------------------------
# setFloat/setDouble must write the exact bits they are handed. toBits() canonicalizes every NaN,
# which makes the write non-conforming for every format that specifies a bit pattern — and the swap
# is invisible under test, since kotlin.test.expect canonicalizes too.
# See R_no_nan_canonicalization in design/requirements.md, D_float_raw_bits.
n=$(grep -c 'toRawBits()' "$ENDIAN" || true)
[ "$n" -eq 2 ] || err "T_float_raw_bits: $ENDIAN calls toRawBits() $n times, expected 2 (setFloat and setDouble) — R_no_nan_canonicalization, D_float_raw_bits"
! grep -q '\.toBits()' "$ENDIAN" \
  || err "T_float_raw_bits: $ENDIAN calls .toBits(), which canonicalizes every NaN — use toRawBits() (R_no_nan_canonicalization, D_float_raw_bits)"

# --- T_javadoc_jar_from_dokka -------------------------------------------------
# Maven Central never looks inside the -javadoc.jar, so an empty one fails nothing and shipped
# unnoticed in every release until 0.4. The jar is fed from Dokka; `javadoc` stays disabled.
# See R_javadoc_jar_has_docs in design/requirements.md, D_dokka_javadoc.
grep -q 'from(tasks\.dokkaGeneratePublicationJavadoc)' "$BUILD" \
  || err "T_javadoc_jar_from_dokka: $BUILD no longer fills javadocJar from dokkaGeneratePublicationJavadoc — the published jar would hold only a manifest (R_javadoc_jar_has_docs, D_dokka_javadoc)"
grep -q 'tasks\.javadoc {' "$BUILD" && grep -q 'enabled = false' "$BUILD" \
  || err "T_javadoc_jar_from_dokka: $BUILD no longer disables the \`javadoc\` task — it has nothing to read but module-info.java (R_javadoc_jar_has_docs, D_dokka_javadoc)"

# --- T_endian_states_bounds ---------------------------------------------------
# dokka-javadoc drops @throws (probed: it survives Dokka's HTML renderer and not its javadoc one,
# while @param and @return survive both), so the ByteArrays.kt tags render for IDE and source
# readers but not in the published -javadoc.jar. The Endian class doc's prose bullet is the one
# statement of the bounds contract that reaches that jar — don't delete it as "duplicated by the
# tags". See R_bounds_contract_published in design/requirements.md, D_kdoc_voice.
grep -q 'Out of range throws' "$ENDIAN" \
  || err "T_endian_states_bounds: $ENDIAN no longer states the out-of-range contract in class-doc prose — the @throws tags do not reach the published javadoc jar (R_bounds_contract_published, D_kdoc_voice)"

if [ "$fail" -eq 0 ]; then
  echo "project tripwires: ok"
fi
exit "$fail"
