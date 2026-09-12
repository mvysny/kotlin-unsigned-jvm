#!/usr/bin/env bash
# Checks that the design/ doc layer is consistent (the layout is described in AGENTS.md, "Design docs"):
#   - every cited decision / requirement slug has its "## " heading in design/
#   - the root AGENTS.md is under 34 KB and every nested AGENTS.md under 10 KB (loaded on every turn)
#   - every CLAUDE.md is exactly the "@AGENTS.md" shim, and one sits beside every AGENTS.md
#   - every cited T_ tripwire slug has a check in a tripwire script, and every check is cited
#   - design/ holds decisions.md, requirements.md and exactly one of solution.md | architecture.md
#   - no index file in design/ideas/, no legacy UPPERCASE doc files at the root
# Run from anywhere inside the repo: design/verify_design_tripwires.sh
# Exit 0 when green, 1 with one "tripwire:" line per failure otherwise.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

fail=0
err() { printf 'tripwire: %s\n' "$*" >&2; fail=1; }

DESIGN=design
[ -d "$DESIGN" ] || { err "no $DESIGN/ folder"; exit 1; }

# Tracked text files only (grep -I skips binaries). NUL-safe.
# Listed once, and an empty listing is a failure: a `git ls-files` that fails inside a loop below
# — no git, an unreadable checkout — would otherwise leave every check with nothing to walk, and
# a vacuous pass is indistinguishable from a green one.
files=()
while IFS= read -r -d '' f; do files+=("$f"); done < <(git ls-files -z)
[ "${#files[@]}" -gt 0 ] || { err "git ls-files listed nothing in $PWD"; exit 1; }
tracked() { printf '%s\0' "${files[@]}"; }

# --- cited slug → heading -----------------------------------------------------
# $1 = prefix letter, $2 = the file whose "## <prefix>_<slug>" headings define them.
check_namespace() {
  local prefix=$1 file=$2 pattern cited defined missing slug
  pattern="\\b${prefix}_[a-z][a-z0-9_]*"
  cited=$(tracked | xargs -0 grep -I -o -h -E "$pattern" 2>/dev/null | sort -u || true)
  if [ ! -f "$file" ]; then
    [ -z "$cited" ] || err "$file is missing but ${prefix}_ slugs are cited"
    return
  fi
  defined=$(grep -o -E "^## ${prefix}_[a-z][a-z0-9_]*" "$file" | sed 's/^## //' | sort -u || true)
  missing=$(comm -23 <(printf '%s\n' "$cited") <(printf '%s\n' "$defined") | sed '/^$/d')
  for slug in $missing; do
    err "\`$slug\` is cited but has no '^## $slug' heading in $file"
  done
}
check_namespace D "$DESIGN/decisions.md"
check_namespace R "$DESIGN/requirements.md"

# --- mandatory files, exactly one assembled picture ----------------------------
[ -f "$DESIGN/decisions.md" ]    || err "$DESIGN/decisions.md is missing"
[ -f "$DESIGN/requirements.md" ] || err "$DESIGN/requirements.md is missing"
n=0
[ -f "$DESIGN/solution.md" ]     && n=$((n + 1))
[ -f "$DESIGN/architecture.md" ] && n=$((n + 1))
[ "$n" -eq 1 ] || err "$DESIGN/ must hold exactly one of solution.md | architecture.md (found $n)"

# --- AGENTS.md is loaded every turn: cap it -----------------------------------
# Root 34 KB; a nested one 10 KB (it loads beside the root when work touches its directory).
ROOT_LIMIT=$((34 * 1024))
NESTED_LIMIT=$((10 * 1024))
while IFS= read -r -d '' f; do
  case "$f" in
    AGENTS.md)   limit=$ROOT_LIMIT ;;
    */AGENTS.md) limit=$NESTED_LIMIT ;;
    *) continue ;;
  esac
  size=$(wc -c < "$f")
  [ "$size" -le "$limit" ] || err "$f is $size bytes; the cap is $limit — garbage-collect by moving, not summarising"
done < <(tracked)

# --- every CLAUDE.md is the shim; every AGENTS.md has one beside it -----------
while IFS= read -r -d '' f; do
  case "$f" in
    CLAUDE.md|*/CLAUDE.md)
      [ "$(cat "$f")" = "@AGENTS.md" ] || err "$f must contain exactly '@AGENTS.md'; move its content to AGENTS.md"
      ;;
    AGENTS.md|*/AGENTS.md)
      shim="$(dirname "$f")/CLAUDE.md"
      [ -f "$shim" ] || err "$shim is missing (the '@AGENTS.md' shim beside $f)"
      ;;
  esac
done < <(tracked)

# --- T_ tripwire slugs: cited from a requirement's "Enforced by", defined by a check -----------
# A "tripwire script" is any tracked file whose path contains "tripwire", other than this one.
# Both directions: a cited T_ with no check is a rule nobody enforces; a check nothing cites is
# a rule nobody can find. design/ideas/ is exempt — an idea names a slug before its check exists.
scripts=() others=()
while IFS= read -r -d '' f; do
  case "$f" in
    "$DESIGN"/verify_design_tripwires.sh|"$DESIGN"/ideas/*) ;;
    *tripwire*) scripts+=("$f") ;;
    *) others+=("$f") ;;
  esac
done < <(tracked)
tpat='\bT_[a-z][a-z0-9_]*'
grep_slugs() { { [ "$#" -gt 0 ] && printf '%s\0' "$@" | xargs -0 grep -I -o -h -E "$tpat" 2>/dev/null | sort -u; } || true; }
cited=$(grep_slugs "${others[@]}")
defined=$(grep_slugs "${scripts[@]}")
for slug in $(comm -23 <(printf '%s\n' "$cited") <(printf '%s\n' "$defined") | sed '/^$/d'); do
  err "\`$slug\` is cited but no tripwire script defines a check for it"
done
for slug in $(comm -13 <(printf '%s\n' "$cited") <(printf '%s\n' "$defined") | sed '/^$/d'); do
  err "\`$slug\` has a check but nothing cites it — cite it beside the invariant it guards, or drop the check"
done

# --- ideas/: ls is the index -------------------------------------------------
for f in README.md readme.md INDEX.md index.md TOC.md; do
  [ -e "$DESIGN/ideas/$f" ] && err "$DESIGN/ideas/$f: no index file — 'ls' is the index"
done

# --- migration completeness: nothing legacy left at the root -------------------
for f in DECISIONS.md decisions.md NOTES.md RESEARCH.md SOLUTION.md SOLUTION_VERIFY.md \
         REQUIREMENTS.md ARCHITECTURE.md DESIGN.md COMPARISON.md IDEAS.md ideas.md; do
  [ -e "$f" ] && err "root $f should live under $DESIGN/ (lowercase)"
done
[ -d ideas ] && err "root ideas/ should be $DESIGN/ideas/"

if [ "$fail" -eq 0 ]; then
  echo "design tripwires: ok"
fi
exit "$fail"
