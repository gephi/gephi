---
name: sentry-triage
version: 1.0.0
description: Triage unresolved Gephi exceptions from Sentry (org "gephi", project "gephi") into a prioritized, read-only report table. Filters out issues that cannot be fixed in Gephi's code (network/firewall/proxy blocks, OpenGL/GPU driver limits, corrupted/mounted installs, AV/cloud-sync file locking, disk full), scores the rest by user/event impact, and dispatches Sonnet sub-agents to investigate root cause and fix direction for the top-priority issues. Never edits code. Use when asked to triage, review, prioritize, or report on Sentry issues/exceptions for Gephi.
---

## Purpose

Turn the raw Sentry backlog into a prioritized, actionable report so a human or a follow-up agent can decide what to fix next, without anyone having to manually reread 50+ issues first.

## Hard rule — read first

**This skill never modifies application code.** No `Edit`/`Write` on any file under `modules/`, `src/`, or similar. Its only job is to produce a report. Any sub-agent it spawns must be told explicitly that it is read-only and must not attempt a fix — only diagnose and suggest a direction. If the user wants a fix implemented, that is a separate, later task.

The only writes this skill performs are: (a) a local report file, and (b) optionally, Sentry comments/status changes on excluded issues — and only after explicit user confirmation (see Step 3).

## Inputs

- Sentry org slug: `gephi`, project slug: `gephi` (confirmed via `find_organizations`/`find_projects` — don't re-discover each run).
- Optional argument: top-N issues to send for deep investigation. Default **12**.
- Issue window: `search_issues` with `period: "90d"` (the tool's max) and `query: "is:unresolved environment:production release:latest"`. Scope is deliberately **production + the current release only**. `release:latest` tracks whatever the current release is at run time, so it never needs hardcoding or updating as Gephi ships new versions. This excludes dev/SNAPSHOT-build noise and avoids re-triaging bugs already fixed in the current release but still open under an older version someone hasn't upgraded off of. The trade-off, worth restating in the report: a regression that only affects users still on an older release won't surface here. If that matters for a given run, rerun without the `release:latest` filter and say so explicitly in the report. State the resolved scope (e.g. "environment:production, release:latest = Gephi 0.11.2 at run time") in the report Summary so it's never ambiguous what was covered.

## Step-by-step process

### 1. Pull the backlog

`search_issues(organizationSlug="gephi", projectSlugOrId="gephi", query="is:unresolved environment:production release:latest", period="90d", sort="freq", limit=100)`

Also pull `is:ignored` and `is:resolved` (limit 25 each) just to know what's already been dispositioned — never re-analyze those.

### 2. Categorize every unresolved issue

Classify each issue using its title, exception type, and **culprit** (package/class/method). Classify by the **specific exception type/message**, not the culprit alone: a single generic handler method (e.g. a downloader's shared `notifyException` callback) can be the culprit for many different underlying failures with very different fixability. Two issues sharing a culprit can legitimately land in different categories/tiers if their exception types differ — a connection timeout through that handler is unfixable network noise, but an HTTP 429 rate-limit through the *same* handler is a `handling gap`, since Gephi's own retry/backoff behavior is directly implicated. If you place two same-culprit issues in different buckets, say so explicitly in the report so it doesn't look like an inconsistency; one line is enough.

Two confirmed exclusion categories plus three more identified from this project's history:

| Category | Signal patterns (examples, not exhaustive — use judgment) |
|---|---|
| **Network / firewall / proxy** | `ConnectException`, `SocketTimeoutException`, `IOException: Connection timed out`, `PacParsingException`, PKIX/certificate path errors, HTTP 4xx/429/timeout from GitHub raw/update URLs, `wpad.dat`. Culprit often in `org.netbeans.modules.autoupdate.*`, `sun.nio.ch.*`, `sun.net.www.protocol.http.*`. |
| **OpenGL / GPU driver** | `GLException`, `glCreateProgram`/shader/context errors, `OpenGLVersionChecker`, native windowing crashes (`WindowsWindow.wndProc`, `jogamp.newt.*`). Culprit in `org.gephi.viz.engine.*` init/context code or `jogamp.*`. |
| **Corrupted / mounted / partial install** | `FileNotFoundException` for a resource inside a `.jar` that ships with the app itself (e.g. `org-gephi-desktop-branding.jar!/...`, `org-netbeans-*.jar!/...`), especially when the path contains `/Volumes/` (running straight from a mounted macOS `.dmg` instead of installing) or an otherwise mangled install path. |
| **AV / cloud-sync file locking** | `IOException`/`FSException` like "file already being used by another process", "could not delete temporary file", "Cannot rename file... New file exists: false", where the path is inside `OneDrive`, `Dropbox`, or similar sync roots, or generically racing against another process (antivirus scan). |
| **Disk full** | `IOException: No space left on device` and equivalents. |

Issues matching these categories are **excluded from the actionable report**: Gephi code cannot fix a user's network policy, GPU driver, broken install location, third-party file lock, or full disk.

**Record a "signal" for every issue, excluded or not** — the exception type plus culprit class/method (e.g. `IOException: No space left on device — Installer$OutputHandler.publish`). Every row in every table gets this, even rows that don't get deep investigation. A bare title is not enough for anyone to act on or verify later without re-opening Sentry.

**Do not silently drop anything.** Every excluded issue still gets listed in an "Excluded — not actionable" appendix with its signal, category, and one-line reason, so the exclusion logic stays auditable without needing Sentry access.

**Decision rule for borderline cases** — the actual test, applied the same way every time: *could Gephi's own code plausibly add a defensive improvement (catch the exception, degrade gracefully, validate/sanitize input) around this, even though the trigger itself is external?*

- If yes: keep it in the actionable report, tagged `handling gap` (not excluded), regardless of how "environmental" the trigger looks.
- If no — there is truly nothing Gephi's code could do differently — exclude it.

Apply this literally. A `PacParsingException` or an HTTP 429 from a rate limit are externally triggered, but Gephi's code could still catch them and fail quietly instead of surfacing a crash, so they belong in the actionable table as `handling gap`, not in the excluded table with a footnote. Reserve real exclusion for cases with no in-app mitigation at all: disk full, GPU below the minimum OpenGL version, a broken/mounted install, third-party file locks, no network route at all.

If you encounter a new recurring pattern that clearly isn't fixable in Gephi's code and doesn't match the categories above, still include it in the actionable report but flag it clearly (e.g. `category: possibly unfixable — new pattern`) rather than silently inventing a new exclusion category. That's a judgment call for the human reviewing the report.

### 3. Confirm exclusions before touching Sentry

Present the exclusion list (issue ID, title, category, users/events) to the user and get explicit confirmation before writing anything to Sentry. This is shared state visible to the whole team — never skip this gate, even on repeat runs.

Once confirmed, for each excluded issue:
- Post a comment via `update_issue` (or the appropriate Sentry write tool) explaining the exclusion, e.g.: *"Triaged: not actionable in Gephi code — [category]. See `<report file>` for details. Marking as ignored."*
- Set status to `ignored`.

If the user doesn't confirm, still finish and write the report — just skip the Sentry writes and note in the report that exclusions were not applied to Sentry.

### 4. Score the remaining (actionable) issues

**Recency buckets** — use these three labels, not raw relative timestamps, so tier claims about recency are directly checkable in the table:
- `Active`: last occurrence <7d ago
- `Recent`: 7–30d
- `Stale`: >30d

**The primary axis is users affected (breadth of impact), not event count.** Event count is a secondary signal, not a tier driver: a handful of users retrying the same failing action hundreds of times ("event storm") is noted separately (`Notes: possible retry loop`), never used to bump a 1-2-user issue above a 50-user issue or vice versa. If you find yourself tiering two similar-shaped issues (e.g. both "many events, few users") several tiers apart, that's a signal event count is leaking into the decision — stop and re-apply the users-first rule.

Assign a priority tier:

- **P0 – Critical**: ≥50 affected users, OR a crash/data-loss on a core workflow (open/save project, import, export) regardless of user count — provided it's `Active` or `Recent`.
- **P1 – High**: 15–49 affected users, `Active` or `Recent`.
- **P2 – Medium**: <15 affected users but still `Active` or `Recent`; or any `handling gap` borderline issue (see Step 2) regardless of user count.
- **P3 – Low**: `Stale` (no occurrence in 30+ days), regardless of how high the historical user/event count was — nobody is currently hitting it.

**Within a tier, sort by users affected descending, then events descending as the tiebreak.** State this sort explicitly in the report so the ranking is reproducible, not just "however they came out."

**Duplicate/related issues**: if two or more issues share a culprit class/method and clearly describe the same underlying bug (e.g. `setX` and `setY` both throwing "cannot be NaN" from the same layout algorithm, or the same watchdog exception and its `InterruptedException` sibling), group them explicitly. Investigate one representative, then apply its root cause, fix direction, and confidence **identically** to every other issue in the group in the table. Never leave some group members filled in and others blank with no explanation.

**Issues already assigned to someone in Sentry**: keep them in the report under a separate "Assigned / in progress" section (still show their tier), but do not spend a sub-agent investigating them — someone's already on it, and re-investigating wastes effort. If an issue would otherwise be *excluded* (Step 2/3) but is already assigned, don't fold it quietly into the excluded table — call it out explicitly (e.g. in a summary note), since marking it `ignored` could clash with in-progress human work on a mitigation.

### 5. Deep investigation for the top-N unassigned issues

Take the top N (default 12, or the user-supplied count) **unassigned** actionable issues by tier. For each:

1. Fetch full detail with `get_sentry_resource` (full stack trace, breadcrumbs, tags, a sample event) — do this yourself, don't make the sub-agent call Sentry.
2. Spawn one sub-agent per issue, **model: sonnet**, read-only tools only (no `Edit`/`Write`/`NotebookEdit`), using this prompt shape:

   > Investigate the root cause of this Gephi exception. Do NOT modify any files — this is investigation only, another task will implement any fix later.
   >
   > **Issue**: `<title/exception>`
   > **Culprit**: `<package.Class#method>`
   > **Stack trace**: `<relevant frames>`
   > **Context**: `<users affected, events, first/last seen, any relevant tags/breadcrumbs>`
   >
   > Use CodeGraph (`codegraph_explore`) if available, otherwise grep/read, to find the relevant code. Report: (1) most likely root cause, (2) the specific file(s)/line(s) involved, (3) one or two concrete directions for a fix (not a diff, not implemented code — just the approach), (4) your confidence (high/medium/low) and why. Keep it under ~200 words.

   Run independent issues' sub-agents in parallel (single Agent tool call with multiple invokes, or background if the count is large).
3. If investigation genuinely can't pin a root cause (e.g. sparse stack trace, third-party library internals with no Gephi frame), report that honestly instead of guessing — "insufficient signal, needs a repro" is a valid finding.
4. **Confidence is a single value: High / Medium / Low.** This is a table cell, not prose, and every row in the column uses this scale. If root-cause confidence and fix-mechanics confidence differ (e.g. you're sure what's broken but less sure of the best fix), report the *lower* of the two in the table and explain the split in the Investigation Details prose — never a compound string like "High / Medium-high" in the cell. Sub-agents will sometimes hand back hedge words like "medium-high" or "high-ish": first round that down to the nearest of the three real buckets (medium-high → Medium), *then* take the lower of root-cause vs. fix-mechanics. Don't let a hedge word sneak a higher value into the table than the rule allows. Check every table cell against this rule before writing the report — it's easy to get wrong while writing up many rows at once.

Actionable issues beyond the top N: still list them in the report table with their Sentry metadata and tier, just without the investigation columns. Their Status cell must say `Not investigated (beyond cap of N)` with the actual N used for that run — not a bare "Not investigated" — so a reader can tell "deliberately out of scope for this run" apart from "this fell through a gap." If a run builds incrementally on a previous one (e.g. investigating 3 more issues on top of an earlier batch), state the cumulative count and the previous report it builds on in the Summary, rather than leaving the reader to infer why the cap looks larger than a single batch.

### 6. Write the report

Write to `reports/sentry-triage/sentry-triage-<YYYY-MM-DD>.md`, creating the directory if needed. This repo is public/open-source, so the path is gitignored — do not remove that `.gitignore` entry, and don't commit report files. If re-running the skill on the same day (e.g. investigating more issues on top of an earlier same-day run rather than starting over), append `-v2`, `-v3`, etc. to the filename and say in the new report's summary which earlier report it builds on — don't silently overwrite same-day history.

Formatting rules:

- Keep the summary table narrow and skimmable — it is a triage index, not the findings themselves. Root cause / fix-direction prose belongs only in the Investigation Details appendix, even when there's very little of it.
- The `Signal` column (exception type + culprit, one line) is mandatory for every row, investigated or not, so nothing is ever a bare title with nothing to verify or act on.
- The `Notes` column is for short tags only, a few words: `duplicate of GEPHI-XXXX`, `possible retry loop`, `handling gap`, `needs investigation to confirm X` — never a full sentence. If a categorization-time observation needs a full sentence (e.g. "this uncaught IOException surfacing as a RuntimeException suggests a missing catch regardless of the underlying cause"), it goes in the Investigation Details appendix as a short "Categorization note" entry for that issue, not stretched across the summary table.
- Include, once, a one-line legend for the recency buckets right in the report (not just in this skill file) — e.g. `Active = last seen <7d, Recent = 7-30d, Stale = >30d` — so the report is self-contained and a reader can check the tiering without opening the skill definition.

Structure:

```markdown
# Sentry Triage Report — <date>

Org/project: gephi/gephi · Window: last 90 days · Generated by: sentry-triage skill

## Summary
- N unresolved issues pulled, X excluded (not actionable), Y actionable
- Top N sent for deep investigation, Z remaining listed without investigation
- Sort within each tier: users affected desc, then events desc
- [Excluded issues marked as `ignored` in Sentry with explanatory comment | Exclusions NOT applied to Sentry — awaiting confirmation]
- Any excluded-but-assigned issues, called out explicitly (see Step 4)

## Actionable issues (prioritized)

Sorted by tier, then users desc, then events desc within tier.

| Tier | Issue | Signal (exception + culprit) | Users | Events | Last seen | Confidence | Status | Notes |
|---|---|---|---|---|---|---|---|---|
| P0 | [GEPHI-XXXX](link) Title | `IllegalArgumentException: x cannot be NaN` — `NodeImpl.setX` | 120 | 1021 | Active | High | Investigated | duplicate: see GEPHI-YYYY |
| P0 | [GEPHI-YYYY](link) Title | `NullPointerException` — `NodeImpl.setY` | 20 | 117 | Active | High | Investigated (duplicate) | duplicate of GEPHI-XXXX |
| P1 | [GEPHI-ZZZZ](link) Title | `IOException: ...` — `Some.Class#method` | 40 | 200 | Recent | — | Not investigated (beyond cap of N) | — |

Every row must have a populated Signal column, even "Not investigated"
ones — that's the minimum a follow-up agent needs to start from without
re-querying Sentry.

## Assigned / in progress (not re-investigated)

| Tier | Issue | Signal | Assignee | Users | Events | Last seen |
|---|---|---|---|---|---|---|

## Excluded — not actionable

| Issue | Signal (exception + culprit) | Category | Users | Events | Reason | Sentry action taken |
|---|---|---|---|---|---|---|

Call out any excluded issue that is also currently assigned to someone,
right in this table's Reason cell as a plain sentence (e.g. "...
Assigned to X — do not auto-ignore.") — don't bury it in a separate
footnote, and don't reach for bold to make it stand out; a table cell is
already the emphasis.

## Investigation details

### GEPHI-XXXX — Title
- **Root cause**: ...
- **Affected code**: `path/to/File.java:123`
- **Suggested direction**: ...
- **Confidence**: High — reasoning (include any root-cause-vs-fix confidence split here, not in the table)
- **Duplicates**: GEPHI-YYYY, GEPHI-ZZZZ (same root cause) — omit if not applicable. Always say "duplicate"/"duplicates" here and in the table, never "cluster" in one place and "duplicate" in another for the same relationship.
- **Sentry**: link

### GEPHI-XXXX — categorization note
Use this shorter form (no Root cause/Affected code/Confidence fields)
for an issue that was *not* deep-investigated but whose categorization
still needed more than the Notes column's few words to explain — e.g.
why it doesn't fit an existing exclusion category, or why it shouldn't
be lumped in with a category it superficially resembles. One short
paragraph, plain prose.
```

### 7. Final message to the user

Short summary only: counts per tier, how many investigated, link to the report file and to the Sentry dashboard. Do not paste the whole report into chat if it's long — point to the file.

### 8. Cleanup

When the PRs are pushed to GitHub, local worktrees should be cleaned up.

## Writing style

The report is read by humans deciding what to fix and by other agents deciding how to fix it. Write it like you're briefing a colleague, not like you're demonstrating thoroughness. Concretely:

- **Don't lean on em dashes as a default connector.** If every second clause is joined with "—", vary it: a period and a new sentence, "because", "which", "so", a comma. An em dash for a genuine aside is fine; an em dash because you didn't pick a real connective word is not.
- **Don't bold for emphasis.** Bold is for the field labels (`**Root cause**:`) the template already defines — not for making a phrase inside a sentence stand out. If something's important, say it plainly; word choice and sentence position do the emphasis.
- **Say it once.** Avoid restating the same qualifier in two forms in one sentence (e.g. "a genuine handling gap, not a fundamental limitation, and something Gephi could actually fix"). Pick the clearest version and cut the rest.
- **Use one term per concept, everywhere.** Decide once whether grouped duplicate issues are called "duplicates" or "a cluster" and use that word in the table, the Notes column, and the Investigation Details section alike. Same for any other recurring concept — don't let synonyms drift in across sections written at different points in the run.
- **Cut hedge-stacking.** "Medium-high confidence, though not fully certain, and somewhat speculative" is three hedges for one idea. Pick the single High/Medium/Low value the confidence rule requires and move the nuance, if any, into one clause of the details prose — not a chain of qualifiers.
- **Vary sentence shape.** If three rows in a row start with "This is a genuine X, not Y" or "Root cause: X happens because Y", a reader notices the template before the content. Rewrite so consecutive entries don't read as filled-in mail merge.
- Before finishing, skim your own draft once for these patterns specifically — they're the easiest tells that a report was written under time pressure rather than edited.

## Guardrails recap

- No code edits, ever — this skill and its sub-agents only read and report.
- No silent drops — every pulled issue ends up in exactly one section of the report (actionable, assigned, or excluded).
- No Sentry writes without explicit confirmation of the exclusion list.
- Investigation sub-agents must say "insufficient signal" rather than fabricate a root cause when the stack trace doesn't support one.
