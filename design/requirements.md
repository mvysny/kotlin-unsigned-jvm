# Requirements

What must hold — the promises the README's pitch makes about this library, as official rules. One
entry per promise. A requirement *states*; it never argues: the fork behind it, if there is one, is
a `D_` entry it cites.

- **Owner-written.** An agent never adds, edits or retires an entry here; it proposes one — in
  conversation, or as a drafted entry in `design/ideas/<slug>.md` — and the owner moves it in.
- A requirement is a promise the README's pitch makes — what this library does differently, or
  better, than `ByteBuffer` and the rest of `design/comparison.md`. The owner's ruler for a
  proposal: **allow the opposite everywhere — is it still the pitched library?** "A NaN keeps its
  payload", "`endian` always defaults to `Endian.Big`" → not the same library → an entry. "The
  `VarHandle`s stay top-level", "the three spellings of the module package match" → a build or a
  benchmark breaks, the library is the same → not an entry: an *invariant* — the rule that *keeps*
  a promise — is an `AGENTS.md` one-liner, named here under *Enforced by*; an API semantic is its
  KDoc and its `D_`; "JVM target 17" is one build line.
- Cite by slug — `R_<slug>`. `grep '^## R_' design/requirements.md` is the index.
- Shape: `## R_<slug> — <the promise in its operational form, one sentence>`, then **Status**
  (Active, or Retired <date> — why it stopped being a promise), **If violated** (the observable
  failure, one or two sentences — not the argument, which is the `D_`'s), **Enforced by** (a test,
  a compiler setting, a tripwire cited as `T_<slug>` — this line is that slug's home — or "review
  only"), **See** (the pitch passage and the `D_` entries behind it).
- A retired promise stays as a tombstone; an entry that was never a promise — an invariant filed
  here by mistake — is deleted outright once its rule has a home. A requirement that wants a
  *Rejected:* section is a decision — move it to `decisions.md`.
- **The first entry is the ruler**: later entries are trimmed to its length, never the other way
  round.

---

## R_unsigned_first_class — Every width has a true unsigned accessor returning a Kotlin unsigned type; the caller never converts

**Status:** Active.
**If violated.** A read comes back as a signed `Int` needing a trailing `.toUInt()` at the call
site — forget one and a `0xFFFFFFFF` field silently becomes `-1`, which is the bug the README's
"Motivation" says this library exists to make impossible. A width without its unsigned counterpart
sends that width's callers back to `ByteBuffer`'s conversion dance, and at 64 bits there is no
widening trick to send them to at all.
**Enforced by.** Review only — `ByteArrayTest` covers every unsigned accessor that exists, but
nothing fails if a new width ships signed-only.
**See.** README "Motivation" (the `ByteBuffer` contrast), `D_jvm_only`.

## R_endian_defaults_big — Every `ByteArray` accessor that takes an `endian` parameter defaults it to `Endian.Big`

**Status:** Active.
**If violated.** A caller who omits `endian` gets one byte order at most call sites and another at
the odd one out — silent data corruption at every site that trusted the pattern, with nothing wrong
to see in the source. Byte-sized accessors take no `endian` at all, since endianness is meaningless
for one byte.
**Enforced by.** `T_endian_default_big`; `ByteArrayTest` asserts the default on each width.
**See.** README "The `endian` value always defaults to `Endian.Big`".

## R_no_nan_canonicalization — The float accessors write the exact bits they are given: `toRawBits`, never `toBits`

**Status:** Active.
**If violated.** A NaN written through `setFloat`/`setDouble` lands in the array canonicalized to
`0x7fc00000` / `0x7ff8000000000000` — non-conformance for every binary format that specifies a bit
pattern, and a destroyed sentinel for the protocols that carry a payload there. The promise is
one-directional: nothing here canonicalizes; what survives a round trip through a `Float` *variable*
is the platform's business.
**Enforced by.** `T_float_raw_bits`.
**See.** README "The bits are never rewritten", `D_float_raw_bits`.
