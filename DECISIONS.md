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

*No entries yet.*
