---
name: release-notes
description: Draft Markdown release notes for a Gephi release (org "gephi", repo "gephi"), ready to copy-paste into a GitHub release. Source of truth is a GitHub milestone the user names when invoking the skill. Pulls every issue and PR in that milestone, prefers the issue over a linked PR, classifies each item into Bugfixes / New or Improved Features / Code Improvements / API Changes / New Features Highlight by title alone, sources API Changes from src/main/javadoc/overview.html for minor releases, and appends New contributors / Core contributors sections. Read-only against GitHub — never creates, edits, or publishes the actual release. Use when asked to draft, prepare, or generate release notes for a Gephi version/milestone.
---

## Purpose

Turn a GitHub milestone into a release-notes draft in the exact shape Gephi has published for its
last several releases, so the maintainer can review, tweak classifications, and paste it straight
into the GitHub release editor — instead of hand-copying issue titles and links one by one.

## Hard rules — read first

- **Never touches GitHub.** No `gh release create`, no milestone edits, no issue/PR comments or
  labels. Output is a Markdown draft file plus the same content printed in chat. Publishing is a
  separate, human-approved step.
- **Issue over PR, always.** If a PR's body closes an issue that is *also* in this milestone, the
  issue gets the bullet and the PR is dropped from the list entirely (no bullet for either the PR
  or a duplicate). Only PRs that don't close an in-milestone issue get their own bullet.
- **Classify by title alone.** Do not open the issue/PR body, diff, or comments to decide the
  category — that defeats the point of keeping this cheap. Titles are enough; the user reviews and
  can correct any misclassification.
- **Only closed issues and merged PRs count.** Anything still open, or a PR that was closed without
  merging, didn't ship — drop it from the draft silently (it doesn't even belong in an appendix,
  since it's simply not part of this release).
- **Categories are not mandatory.** Only emit a section (`###` heading) if at least one item landed
  in it. A patch release with zero new features has no "New or Improved Features" heading at all.
- **API Changes only for minor releases**, sourced from `src/main/javadoc/overview.html`, not from
  title classification — see Step 6.
- **Core contributors is a fixed allow-list**: `@eduramiba`, `@totetmatt`, `@mbastian`, `@jacomyma`.
  Never add anyone else to that section, and only list the ones who actually authored a merged PR
  in this milestone (a quiet release may only have one of the four).

## Inputs

- Repo: `gephi/gephi` via `gh` CLI (already authenticated in this environment).
- **Milestone**: the user supplies this when kicking off the skill (e.g. `0.11.3`, or a milestone
  number). If they only say a version number, match it against milestone `title`, not `number`.

## Step-by-step process

### 1. Resolve the milestone

```
gh api "repos/gephi/gephi/milestones?state=all&per_page=100" --paginate --jq '.[] | select(.title=="<version>")'
```

Milestones close on release, so `state=all` is required — the default `state=open` misses every
past release. If nothing matches, or more than one milestone matches the given title, stop and ask
the user to disambiguate rather than guessing.

### 2. Fetch every issue and PR in the milestone

```
gh api "repos/gephi/gephi/issues?milestone=<number>&state=all&per_page=100" --paginate
```

This one endpoint returns both issues and PRs (a PR shows up with a `pull_request` sub-object).
For each item, keep: `number`, `title`, `html_url`, `state`, `body`, `user.login`, and — when
present — `pull_request.merged_at`.

Filter to what actually shipped:
- Plain issues: keep only `state == "closed"`.
- PRs (has `pull_request`): keep only when `pull_request.merged_at` is non-null. A PR closed
  without merging didn't ship — drop it.

**Known gap**: some merged PRs are never assigned a milestone at all — Dependabot bumps are the
main repeat offender (confirmed missing from the 0.11.2 milestone despite shipping in that
release). Since the milestone is the deliberate source of truth here, this skill doesn't try to
compensate by also diffing tags/commit ranges — it's a one-line gap the maintainer can patch in by
hand, and worth a one-line mention in the final chat summary.

### 3. Prefer the issue over a linked PR

For every surviving PR, scan its `body` for GitHub's closing keywords referencing an issue number:

```
(?i)\b(close[sd]?|fix(?:e[sd])?|resolve[sd]?)\b[^\n#]{0,20}#(\d+)
```

If any referenced number matches an issue kept in Step 2, drop this PR from the item list — the
issue already carries the bullet. If the PR references an issue that is *not* in this milestone (or
references nothing), keep the PR as its own item.

Dependabot PRs (`user.login == "dependabot[bot]"`) never close an issue — always keep them as PR
items.

**PR bodies are frequently empty in this repo** (squash-merge with no template filled in), so the
keyword regex alone under-detects. Also check the PR **title** for a leading tracking-number
prefix — this repo has a convention of titling a PR `#NNNN <description>` where `#NNNN` is the
issue it addresses (e.g. `#3025 Fix crash when Dynamic Range filter is used as a subquery`). Treat
that leading `#NNNN` exactly like a body-based closing reference: if `NNNN` is an issue kept in
Step 2, drop the PR in favor of the issue. Either way — dropped or kept — strip the leading `#NNNN
` from the title before using it in a bullet; it's an internal tracking artifact, not part of the
human-readable title, and none of the historically published release notes show it.

Not every same-fix PR/issue pair is actually linked by either mechanism (no body keyword, no title
prefix) — when that happens the issue and its fixing PR(s) will both surface as separate, visibly
overlapping bullets. Don't try to infer the link from title similarity; that's a step beyond
"cheap and title-only." Leave the overlap for the user to dedupe during review, and mention in the
final chat summary if a release had a noticeable cluster of this kind.

### 4. Classify each surviving item by title alone

Read through the list of titles (issues and remaining PRs together) and sort each into one bucket:

| Category | What belongs here (title-level signal) |
|---|---|
| **Bugfixes** | "Fix", "crash", exception names (NPE, IllegalArgumentException, ...), "doesn't work", "wrong", "incorrect", regressions |
| **New or Improved Features** | "Add", "New", user-facing capability or UI addition/improvement |
| **Code Improvements** | Refactors, dependency/version bumps, upgrades, internal cleanup, CI/build/release-workflow changes, performance work with no user-visible behavior change |
| **API Changes** | Not classified from titles — see Step 6 |
| **New Features Highlight** | A subset of "New or Improved Features" for minor releases only — see Step 5 |

This is a judgment call — titles are sometimes ambiguous (e.g. "Improve X" could be a feature or a
code improvement depending on whether X is user-visible). Pick the best fit; the user reviews the
draft and can move things.

### 5. Decide minor vs. patch, and pick the section set

Parse the target version as `X.Y.Z`. It's a **minor release** if `Z == 0`, OR if no earlier release
exists for the same `X.Y` with a smaller `Z` (check via `gh release list`) — this covers cases like
Gephi 0.11.1, which is the first actual release of the 0.11 line even though it isn't `.0`.
Otherwise it's a **patch release**.

Section order is always **features first, then bugfixes, then code improvements** (API Changes, if
present, comes last as a dev-facing appendix):

- **Minor release** sections, in this order: `New Features Highlight`, `Other New or Improved
  Features`, `Bugfixes`, `Code Improvements`, `API Changes`. Split the features bucket from Step 4:
  highlight only the handful that are genuinely significant (new engines/subsystems, major new
  panels or workflows, things worth a release headline) — everything else user-facing goes to
  "Other New or Improved Features". Keep the highlight list short (roughly 3-7 items); when in
  doubt, it's not a highlight.
- **Patch release** sections, in this order: `New or Improved Features`, `Bugfixes`, `Code
  Improvements`. No highlight split, no API Changes section, even if Step 4 turned up a feature or
  two — small releases do occasionally ship a minor feature alongside bugfixes.

### 6. API Changes (minor releases only)

Read `src/main/javadoc/overview.html`. Find the `<h3>` heading matching the target version (try the
exact version string, e.g. `0.11.0`). Its content runs until the next `<h3>` or `<hr>`, organized by
`<h4>` per API module.

Convert that HTML slice to Markdown, preserving the per-module structure:

```markdown
#### <Module> API

- <bullet text, `<code>` tags become backticks>
```

If no `<h3>` section matches the target version, don't fabricate one — note in the draft (as an
HTML comment or a bracketed TODO right where the section would go) that `overview.html` has no
entry yet for this version, so the maintainer knows to add API-change entries before publishing, or
confirms there genuinely are none.

### 7. New contributors

For every unique non-bot PR author among this release's items, check whether this is their first
merged PR in the repo:

```
gh api "search/issues?q=repo:gephi/gephi+is:pr+is:merged+author:<login>&sort=created&order=asc&per_page=1" --jq '.items[0].number'
```

If the earliest merged PR number returned equals one of the PR numbers landed in this milestone,
they're a new contributor — credit them on that PR. If someone has more than one first-time PR in
the same milestone, list both, matching the existing style:

```
- **@user** made their first contribution in [#NNNN](https://github.com/gephi/gephi/pull/NNNN)
```

Omit this whole `## New contributors` section if nobody qualifies.

### 8. Core contributors

Intersect the fixed allow-list (`@eduramiba`, `@totetmatt`, `@mbastian`, `@jacomyma`) with the set
of PR authors (`user.login`) who landed a merged PR in this milestone. List only the ones present,
comma-separated with "and" before the last: `@a, @b and @c`. If none of the four contributed, omit
the section rather than listing nobody.

### 9. Assemble the draft

Follow the Output format template below. Write to
`reports/release-notes/<version>.md` (create the directory if needed; add `/reports/release-notes/`
to `.gitignore` if not already present — this repo is public, don't commit drafts). Also print the
full draft in chat so it can be copy-pasted immediately without opening the file.

## Output format template

Patch release:

```markdown
Follow-up release from `<previous-tag>` with bugfixes.

## What's Changed

### New or Improved Features

- <title> [#NNNN](https://github.com/gephi/gephi/issues/NNNN)

### Bugfixes

- <title> [#NNNN](https://github.com/gephi/gephi/issues/NNNN)
- <title> [#NNNN](https://github.com/gephi/gephi/pull/NNNN)

### Code Improvements

- <title> by @dependabot[bot] [#NNNN](https://github.com/gephi/gephi/pull/NNNN)

## Core contributors

@mbastian

If you're looking for digital signatures for the release binaries, you can directly find them on [Maven Central](https://repo1.maven.org/maven2/org/gephi/gephi/<version>/)
```

Minor release:

```markdown
## What's Changed

### New Features Highlight

- <title> [#NNNN](https://github.com/gephi/gephi/issues/NNNN)

### Other New or Improved Features

- <title> [#NNNN](https://github.com/gephi/gephi/issues/NNNN)

### Bugfixes

- <title> [#NNNN](https://github.com/gephi/gephi/issues/NNNN)

### Code Improvements

- <title> [#NNNN](https://github.com/gephi/gephi/issues/NNNN)

### API Changes

#### <Module> API

- <bullet from overview.html>

## New contributors

- **@user** made their first contribution in [#NNNN](https://github.com/gephi/gephi/pull/NNNN)

## Core contributors

@eduramiba, @totetmatt, @mbastian and @jacomyma

If you're looking for digital signatures for the release binaries, you can directly find them on [Maven Central](https://repo1.maven.org/maven2/org/gephi/gephi/<version>/)
```

Bullet-link convention (applies within any category, and is the same regardless of whether the
item is sourced from an issue or a PR — always `[#NNNN](url)`, never a bare "in <url>"):
- Item sourced from an issue → `<title> [#NNNN](issue html_url)`.
- Item sourced from a PR authored by a regular contributor → `<title> [#NNNN](pr html_url)` (no
  `by @author`, since the maintainer publishing the release is usually the PR author too).
- Item sourced from a bot PR (dependabot, etc.) → `<title> by @<bot-login> [#NNNN](pr html_url)` —
  keep the attribution since it's automated and worth flagging as such.

## Final message to the user

Short summary only: version, minor/patch, item counts per category, new-contributor count, path to
the draft file. Don't repeat the whole draft in the summary — it was already printed once above.

## Writing style

Titles are kept verbatim from GitHub — don't rewrite or "clean up" wording, even if a title looks
redundant under its category heading (e.g. "Fix ..." under Bugfixes). Write for a maintainer who
will proofread every line before publishing; the goal is a fast, faithful draft, not a polished
final copy.

## Guardrails recap

- Read-only against GitHub — no releases created, no milestones/issues/PRs modified.
- Issue always wins over a PR that closes it; never show both.
- Titles only for classification — no body/diff reads to decide category.
- Unshipped items (open issues, unmerged PRs) never appear, not even in an appendix.
- Empty categories are omitted, not shown with "None".
- Core contributors section only ever contains a subset of the fixed four handles.
