#!/usr/bin/env bash
# This project's own mechanical checks — one per rule that compiles, passes every test, and is wrong
# anyway. Each guards a promise or an invariant stated in AGENTS.md; the message names it.
# The doc-layer checks (slug resolution, the AGENTS.md cap, the CLAUDE.md symlink, the verbatim
# pitch) are the other script: design/verify_design_tripwires.sh.
#
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

# --- the VarHandles stay top-level -------------------------------------------
# The six byte-array-view VarHandles must be top-level `private val`s: C2 folds a VarHandle access
# into one instruction only while the handle is a static final field. Moving them into the enum as
# instance fields compiles, passes every test, and is slower than the shifts they replaced.
# See the AGENTS.md invariant, D_varhandle.
n=$(grep -c '^private val [A-Z_]*: VarHandle =$' "$ENDIAN" || true)
[ "$n" -eq 6 ] || err "$ENDIAN declares $n top-level \`private val …: VarHandle\`, expected 6 — they must NOT move into the enum (AGENTS.md invariant, D_varhandle)"

# --- the three spellings of the module package agree --------------------------
# The JPMS module name, its exports, and the --patch-module target must name the same package,
# or javac rejects module-info.java with "package is empty or does not exist".
# See the AGENTS.md invariant, D_patch_module.
pkg_module=$(sed -n 's/^module \([a-z0-9._]*\) {.*/\1/p' "$MODULE_INFO")
pkg_exports=$(sed -n 's/^ *exports \([a-z0-9._]*\);.*/\1/p' "$MODULE_INFO")
pkg_patch=$(sed -n 's/.*"--patch-module", "\([a-z0-9._]*\)=.*/\1/p' "$BUILD")
for got in "$pkg_exports" "$pkg_patch"; do
  [ -n "$got" ] || err "could not find the package name in $MODULE_INFO / $BUILD — did the syntax change? (AGENTS.md invariant)"
done
[ "$pkg_module" = "$pkg_exports" ] || err "module is \`$pkg_module\` but exports \`$pkg_exports\` (AGENTS.md invariant, D_patch_module)"
[ "$pkg_module" = "$pkg_patch" ]   || err "module is \`$pkg_module\` but --patch-module targets \`$pkg_patch\` (AGENTS.md invariant, D_patch_module)"

# --- every endian parameter defaults to Endian.Big ----------------------------
# One overload defaulting to Little would silently corrupt data at every call site that trusted the
# pattern. See the AGENTS.md promise "`endian` is an ordinary argument, defaulting to Endian.Big".
total=$(grep -c 'endian: Endian' "$BYTEARRAYS" || true)
defaulted=$(grep -c 'endian: Endian = Endian\.Big' "$BYTEARRAYS" || true)
[ "$total" -eq "$defaulted" ] || err "$BYTEARRAYS has $total \`endian: Endian\` parameters but only $defaulted default to Endian.Big (AGENTS.md promise)"

# --- the float setters never canonicalize -------------------------------------
# setFloat/setDouble must write the exact bits they are handed. toBits() canonicalizes every NaN,
# which makes the write non-conforming for every format that specifies a bit pattern — and the swap
# is invisible under test, since kotlin.test.expect canonicalizes too.
# See the AGENTS.md promise "The bits are never rewritten", its invariant, and D_float_raw_bits.
n=$(grep -c 'toRawBits()' "$ENDIAN" || true)
[ "$n" -eq 2 ] || err "$ENDIAN calls toRawBits() $n times, expected 2 (setFloat and setDouble) — AGENTS.md promise, D_float_raw_bits"
! grep -q '\.toBits()' "$ENDIAN" \
  || err "$ENDIAN calls .toBits(), which canonicalizes every NaN — use toRawBits() (AGENTS.md promise, D_float_raw_bits)"

# --- the javadoc jar is Dokka's HTML ------------------------------------------
# Maven Central never looks inside the -javadoc.jar, so an empty one fails nothing and shipped
# unnoticed in every release until 0.4. The jar is fed from Dokka's HTML renderer — its javadoc one
# silently drops every @throws tag — and `javadoc` stays disabled.
# See the AGENTS.md invariant, D_dokka_html.
grep -q 'from(tasks\.dokkaGeneratePublicationHtml)' "$BUILD" \
  || err "$BUILD no longer fills javadocJar from dokkaGeneratePublicationHtml — the published jar would hold only a manifest, or would lose the @throws tags to the javadoc renderer (AGENTS.md invariant, D_dokka_html)"
grep -q 'tasks\.javadoc {' "$BUILD" && grep -q 'enabled = false' "$BUILD" \
  || err "$BUILD no longer disables the \`javadoc\` task — it has nothing to read but module-info.java (AGENTS.md invariant, D_dokka_html)"

if [ "$fail" -eq 0 ]; then
  echo "project tripwires: ok"
fi
exit "$fail"
