---
name: sentry-remediate
version: 1.0.0
description: Implement fixes for Gephi Sentry issues that have a code-confirmed root cause, one isolated git worktree per duplicate group. Writes a minimal fix and a regression test, runs a scoped build including the checkstyle profile, opens a PR from the repo's template, has a second agent adversarially review the diff, then watches CI until every PR is green. Never merges. Issues whose root cause doesn't hold up on re-inspection are reported back unfixed rather than forced. Use after sentry-triage has produced a report, or when given a specific list of Sentry issues to fix.
---

## Purpose

Turn a triaged, high-confidence Sentry finding into a real, mergeable, CI-green PR, without one fix's exploration stepping on another's and without shipping a fix that only looks right.

This is the companion skill to `sentry-triage`: that skill investigates and reports, read-only; this one implements, verifies, and opens PRs. Keep the split. Don't let this skill re-triage the whole backlog, and don't let `sentry-triage` start editing files.

## Vocabulary

Three terms, one fixed meaning each, used consistently here and in `sentry-triage`:

- **Duplicate group**: one or more Sentry issues sharing a single root cause. This is the unit of work. One worktree, one fix, one PR per group, never per Sentry ID.
- **Dossier**: everything known about a group before any code is touched. The Sentry IDs and URLs, user/event counts, the exception type and stack trace of a representative event, and the prior investigation's root-cause narrative, affected `file:line`, and suggested direction. Usually a `sentry-triage` report entry plus a `get_sentry_resource` call for the trace, sometimes the user's own description of the bug.
- **Flagging** an issue: naming it in the final report with the reason it wasn't fixed. No branch, no PR, no Sentry write. This skill never changes Sentry issue state. That is `sentry-triage`'s job, and only with user confirmation.

## Hard rules — read first

- **One git worktree per duplicate group, always.** Never work two unrelated groups in the same working tree, even sequentially. Parallel remediation is the point, and shared state between fixes is exactly what causes the conflicts this skill exists to avoid.
- **Re-verify the root cause against current HEAD before touching any file, every time, even same-day.** A dossier is a starting point, not ground truth. If the code has moved, or the fix isn't actually safe and minimal once you're looking at it directly, stop without editing anything and flag it. Forcing a speculative fix onto an issue that turns out to be unclear is worse than leaving it flagged, and it is the one thing this skill must never do.
- **A PR isn't done until it's green, and green includes checkstyle.** `mvn compile` passing locally is not the bar. CI runs `-P enableCheckStyle` and will fail on things a plain compile never sees (step 2 covers why). Don't hand back a PR you haven't watched go green.
- **Never merge a PR.** Merging is a human decision. This skill's job ends at "open, reviewed, green."
- **Never force-push, never push to `master`/`main` directly, never skip hooks.**
- **This is a real, public, shared repository.** Every push and PR is visible to maintainers immediately. No internal or employer tooling references, and no private triage-process details in commit messages or PR bodies. The Sentry issue URL (`gephi.sentry.io`) is fine, it's the project's own tracker.
- **Keep PRs small and scoped to the reported group.** If review turns up a related but distinct latent bug at a different call site, flag it in a PR comment. Don't fold it into this PR's diff.

## Inputs

- Sentry org slug `gephi`, project slug `gephi`, same as `sentry-triage`.
- **Source of issues to remediate**, one of:
  - The most recent `sentry-triage` report in `reports/sentry-triage/`: latest date, and within a date the highest `-vN` suffix, since triage appends `-v2`/`-v3` for same-day reruns rather than overwriting. Pull the `Investigation details` entries. By default only remediate entries the report marked **Confidence: High**. Medium and Low need an explicit user go-ahead per issue, because the report is already flagging extra uncertainty about them.
  - An explicit list of Sentry issue IDs or duplicate groups from the user. This is the common case: a human picks a curated subset off a report, for example `GEPHI-5KC / 5RY / 5PG — ForceAtlas2 NaN coordinates`. If an issue isn't in any existing report, or its writeup is too thin to act on, build the dossier yourself first (`get_sentry_resource` for the stack trace and breadcrumbs) before dispatching an agent. Never hand a sub-agent a bare issue title.

Triage reports don't state the Maven module, so derive it from the dossier's affected path: it's the `modules/<Module>/` prefix. Pass it explicitly rather than letting the agent guess.

## Preflight checks

Run these once, up front, rather than discovering a problem on the first failed push.

- `git remote -v` should point at `gephi/gephi`, and `gh auth status` should show an account with push rights. If you're working from a fork instead, every `gh` command below needs a `--repo gephi/gephi` and the PR needs `--head owner:branch`. Labeling also needs triage permission on `gephi/gephi`, which a fork contributor won't have, so drop the `--label` flags in that case and note it in the final report rather than letting `gh pr create` fail.
- `master` should be current: `git fetch origin && git log -1 origin/master` against local, since every worktree branches off it.

## Step-by-step process

### 1. Group and scope

Group the issues the dossier already identifies as duplicates or same-root-cause. One worktree, one fix, one PR per duplicate group. Mention every issue ID in that PR's description so traceability isn't lost.

Scan the affected-file lists across groups; the triage report's "Affected code" lines make this fast. Two groups touching the same file is not disqualifying, worktrees make the parallel edits safe, but note it in the final report since whichever PR merges second will need a routine rebase.

### 2. Remediate: one agent per duplicate group, isolated worktree

Dispatch one sub-agent per group with `isolation: worktree`, via the `Agent` tool or `agent(..., {isolation:'worktree'})` inside a `Workflow` script. See "Orchestration" below for which.

**Model: inherit the session model, don't downgrade. Alternatively, follow the model choice the user designates**.

Fill in the bracketed values and give the agent this prompt. Pass the **full** stack trace, not the few frames the triage report quoted: the agent needs the whole thing to tell a Gephi frame from a JDK or NetBeans one, and it goes into the PR body verbatim.

> Fix this Gephi exception/crash. You are in your own git worktree, branched off `master`. Do the numbered steps in order.
>
> **Sentry issues**: `<IDs>` (`<URLs>`)
> **Impact**: `<N users, M events>`
> **Exception**: `<type and message>`
> **Stack trace**:
> ```
> <full stack trace of a representative event>
> ```
> **Suspected root cause**: `<narrative from the dossier>`
> **Suspected location**: `<path/to/File.java:123>`
> **Suggested direction**: `<from the dossier>`
> **Build scope**: `-pl modules/<Module1>,modules/<Module2>`
> **Branch to create**: `fix/<short-descriptive-slug>`
>
> The root cause and location above come from an earlier investigation. Verify them, don't trust them.
>
> 1. **Verify.** Read the current code at that location. If the root cause doesn't hold, or a safe minimal fix isn't obvious once you're looking at it, stop here: no edits, no branch, no PR. Return `attempted: false` with what the code actually shows and why. This is an expected outcome, not a failure to push through.
> 2. **Fix.** The smallest safe change that addresses the mechanism, not the symptom. Match the file's existing style. No unrelated refactors, no speculative abstractions, no comments restating what the code does.
> 3. **Test, if feasible.** Check the module's existing test conventions first: test directory, framework, and whether AWT/Swing/native-timing dependencies make the bug non-deterministic to reproduce. Add a focused regression test when it's reasonably possible. If it isn't, skip it and say why in the PR rather than forcing one.
> 4. **Build: compile, then checkstyle, then tests.**
>    ```bash
>    mvn -q -pl modules/<Module1>,modules/<Module2> -am compile -DskipTests
>    mvn -q -pl modules/<Module1>,modules/<Module2> -am validate -P enableCheckStyle
>    mvn -q -pl modules/<Module1>,modules/<Module2> -am test
>    ```
>    Checkstyle catches things `compile` never sees: the `check` goal is bound to the `validate` phase and only runs under the `enableCheckStyle` profile, so a plain compile tells you nothing about it. Two specifics worth knowing before you hit them:
>    - Test sources are checked too (`includeTestSourceDirectory` is on). A new test file that copies a neighboring file's license header without a blank line before `package ...;` compiles fine and fails `EmptyLineSeparatorCheck`.
>    - The console prints no rule detail (`consoleOutput` is off). The failing file, line, and rule are in the result XML: `grep -B2 'severity="error"' modules/<Module>/target/checkstyle-result.xml`.
>
>    Fix forward and re-run `validate`. Never add a checkstyle suppression or a test-skip flag to get past it, and don't leave it for CI to find once the PR is already open.
> 5. **Commit, push, open the PR.** Commit message explains *why*, ending with the `Co-Authored-By:` trailer. Then:
>    ```bash
>    git push -u origin <branch>
>    gh pr create --base master --title "<title>" --body "<body>" \
>      --label "<Component>" --label "Sentry"
>    ```
>    Title is a descriptive summary with no issue number, per CONTRIBUTING.md's "PR format" section, which says to omit the number when there's no GitHub issue. The body must reproduce `.github/pull_request_template.md`'s structure: Description, Checklist, Added tests?, Added to documentation?. Fill the Description as described below.
>    - One line of user-visible impact, citing the user and event counts above.
>    - The mechanism: the actual sequence of calls and state that leads to the failure, with real `file:line` references. Walk the reader through *how* the bug happens, not just that it does.
>    - The stack trace, in a collapsed block right after the mechanism so it doesn't push the prose out of view. Paste it verbatim, don't retype or trim frames; a maintainer needs to match it against what they see in Sentry.
>      ````markdown
>      <details>
>      <summary>Stack trace (GEPHI-XXXX)</summary>
>
>      ```
>      java.lang.IllegalArgumentException: x cannot be NaN
>          at org.gephi.graph.impl.NodeImpl.setX(NodeImpl.java:123)
>          ...
>      ```
>
>      </details>
>      ````
>      The blank lines around the inner fence are required, otherwise GitHub renders the block as literal text.
>    - The fix: what changed, and why it's the minimal safe correction rather than a symptom patch.
>    - The Sentry issue links, including every duplicate this one fix covers.
>    - Tick the template's boxes honestly. Check "Added tests? no" only with a real reason, such as a native or timing race, or no test harness in the module. Don't leave it unstated either way.
>
>    **Labels: at most two component labels, plus `Sentry`.** Pick the component from the user-facing feature the failure sits in, read off the stack trace, not from the directory you edited. A `GraphAPI` fix reached from a project load is `IO`, not a graph label. Use a second label only when the trace genuinely spans two areas, which is common: a layout property failing during project save is `Layouts` + `IO`, an OpenGL context failure is `Visualization` + `OpenGL`, a stats dialog NPE is `Statistics` + `UI`.
>
>    | Feature area in the trace | Label |
>    |---|---|
>    | Project save/load, `.gephi` files, import, export | `IO`, plus `Import` or `Export` if that's the specific path, or `GEXF`/`GraphML`/`PDF`/`SVG`/`Spreadsheet` for a format-specific parse failure |
>    | Layout algorithms and their properties | `Layouts` |
>    | Filters | `Filters` |
>    | Statistics and metrics | `Statistics` |
>    | Preview and its rendering | `Preview` |
>    | Data laboratory tables | `Data Laboratory` |
>    | Appearance, ranking, partition | `Appearance` or `Partition` |
>    | Timeline, dynamic graphs | `Timeline` or `Dynamics` |
>    | Graph canvas, OpenGL, NEWT | `Visualization`, plus `OpenGL` for a driver or context failure |
>    | Selection and editing tools | `Tools` |
>    | Swing, EDT, dialogs, window system, docking | `UI` |
>    | Workspace and project lifecycle | `Workspaces` |
>    | App launch, settings upgrade, module loading | `Startup`, or `Installation` for a broken install path |
>    | Database drivers | `Databases` |
>    | Locale, resource bundles, translations | `Localization` |
>
>    Use the label names exactly as written; they are case- and space-sensitive (`Data Laboratory`, not `data-laboratory`), and `gh pr create` fails outright on a name that doesn't exist. If nothing in the table fits, apply only `Sentry`, and say in your returned JSON which label you'd have wanted. Don't invent a label. Don't reach for a severity label (`Critical`/`High`/`Medium`/`Low`) or a triage-workflow label (`Confirmed`, `Fix Committed`, `To review`); those are for issues, not PRs.
> 6. **Return only this JSON**, no prose around it:
>    ```json
>    {
>      "attempted": true,
>      "prUrl": "https://github.com/gephi/gephi/pull/NNNN",
>      "branch": "fix/some-slug",
>      "filesChanged": ["modules/Foo/src/main/java/.../Foo.java"],
>      "testAdded": true,
>      "labels": ["Layouts", "IO", "Sentry"],
>      "summary": "2-4 sentences: the mechanism you confirmed, what the fix does, whether a test was added and why not if it wasn't.",
>      "notAttemptedReason": null
>    }
>    ```
>    If you stopped at step 1, set `attempted: false`, `prUrl`/`branch`/`summary` to `null`, and `filesChanged`/`labels` to `[]`, and put the full explanation in `notAttemptedReason`.

The build commands above are scoped variants of CONTRIBUTING.md's "Building and running" commands. If you need to reproduce CI exactly rather than quickly, it runs `mvn -T 4 --batch-mode -Djava.awt.headless=true verify -P enableCheckStyle` over the whole repo.

### 3. Independently review every opened PR

For each PR that got opened, dispatch a **separate** sub-agent, one that did not write the fix, to review it adversarially:

> Review this PR adversarially. You did not write it. Your job is to find out whether it actually fixes the bug, not to approve it.
>
> **PR**: `<url>`
> **The defect it claims to fix**: `<mechanism from the dossier>`
> **Sentry issues**: `<IDs>` (`<URLs>`)
> **Stack trace**:
> ```
> <full stack trace of a representative event>
> ```
>
> 1. Run `gh pr diff <url>` and review the actual change, not the author's account of it.
> 2. Does the diff address the defect mechanism, or does it only look plausible? **Check behavior, not shape.** If the fix depends on how a library or framework method behaves, verify that behavior by reading its source or decompiling with `javap` rather than assuming the new guard changes anything. A guard that is always true, or always false, exactly on the crashing path is a no-op, and "the diff looks reasonable" will not catch that. It is the most common way a wrong fix survives every earlier check.
> 3. Is it minimal, with no unrelated changes, and consistent with the surrounding file's style?
> 4. Grep for sibling call sites with the same defect shape that the fix didn't cover.
> 5. Post **one** comment (`gh pr comment <url> --body "..."`) only if you have something concrete: a real gap, a missed call site, or evidence the fix doesn't work. A missed sibling call site is worth a comment; it is not grounds for you or anyone else to widen this PR's diff. Don't comment just to say it looks fine.
> 6. Return only this JSON:
>    ```json
>    {
>      "verdict": "correct",
>      "reasoning": "2-4 sentences, citing what you actually checked.",
>      "commentPosted": false,
>      "siblingCallSites": []
>    }
>    ```
>    `verdict` is one of: `correct`, the diff fixes the mechanism; `concerns`, something is wrong or missing, explained in `reasoning`; `not_applicable`, there was no PR to review because remediation returned `attempted: false`, or the PR was withdrawn. Never use `not_applicable` to avoid forming an opinion on a PR that exists.

**A `concerns` verdict saying the fix doesn't work is a bug report against your own PR.** Handle it like any other bug:

1. Read the reviewer's evidence and confirm it independently.
2. Implement a fix that addresses the actual mechanism, in the same worktree and branch.
3. Re-validate: checkstyle and tests.
4. Push a follow-up commit. Never force-push over the wrong version; the history is the record.
5. Update the PR description to describe the corrected mechanism. Stale prose about the abandoned approach is worse than no description.
6. Post one follow-up comment naming what the review caught and what changed.

Rewriting the description without a comment reads as if the mistake never happened. Say it plainly instead, so a maintainer coming to the PR cold knows which version they're looking at. If the concern is only a related bug elsewhere, leave the PR as it is and carry the sibling finding into the final report.

### 4. Babysit CI until every PR is green

A PR being open is not a PR being done. Poll with `gh pr checks <url>`, or `gh pr checks <url> --watch --interval 15`, until each PR's checks resolve, then triage any failure:

- **Failure in a module or file you didn't touch**: almost certainly pre-existing flakiness, such as a randomized-algorithm test asserting exact floating-point equality. Confirm the failing test's file isn't in `filesChanged`, then `gh run rerun <runId> --failed` once. If it fails again the same way, stop and flag it to the user rather than retrying in a loop. A genuinely broken `master` is not this skill's job to fix.
- **Failure touching a file you changed**: it's yours. Pull the log with `gh run view <runId> --log-failed`, reproduce locally with the scoped commands from step 2 including the checkstyle profile, fix, commit, push, re-poll.

A group counts as done only once its PR shows a green `build_and_test` check.

### 5. Clean up the worktrees

Once every PR is green, remove that group's worktree so the user can check the branch out normally:

```bash
git -C <worktree> status -s                  # must be empty
git -C <worktree> rev-parse HEAD             # must equal...
git -C <worktree> rev-parse origin/<branch>  # ...this
git worktree remove <worktree>               # only once both checks pass
```

Never remove a worktree with uncommitted changes or a HEAD that isn't fully pushed. That's unpushed work, not scratch state, so investigate instead. Leave alone any worktree this run didn't create: check the branch against your own list before removing anything. Flagged-only groups never had a worktree, so there's nothing to clean up for them.

### 6. Final report to the user

Chat only, no report file. The PRs are the durable artifact, and `.gitignore` covers `reports/sentry-triage/` but has no entry for this skill. If you ever do persist a report here, add the gitignore entry in the same change.

Lead with a table: duplicate group, PR link, review verdict, CI status. Then call out explicitly:

- Which groups were remediated.
- Which were flagged and why, with no fix forced on them.
- Any correction made after review, described as plainly as the PR comment did.
- Any sibling defects left as PR comments for a human to judge.
- Any two groups touching the same file, since the second to merge needs a rebase.

Link the PRs, don't paste diffs.

## Writing style

PR descriptions and comments are public and permanent, read by maintainers who have never seen this skill. `sentry-triage`'s "Writing style" section applies here in full: same rules, higher stakes, since its output is a local gitignored report and this skill's output lands on a shared repository. The short version:

- Don't lean on em dashes as a default connector. Vary it: a period and a new sentence, "because", "which", "so", a comma.
- Don't bold for emphasis. Bold is for the PR template's own field labels.
- Say it once. Don't restate the same qualifier in two forms in one sentence.
- One term per concept, across the description, the commit message, and the comments.
- Cut hedge-stacking. Make the claim and state it.

Two additions specific to this skill's output:

- Write for a maintainer, not for the review record. "This guard is a no-op on the crashing path because `getX()` returns `0f`, never null" is useful. "Thanks for the excellent catch, great observation" is noise.
- Don't describe your own process. No mention of agents, sub-agents, triage runs, or how the work was orchestrated. The PR is about the code.

## Orchestration: Agent tool vs Workflow tool

For a couple of duplicate groups, plain parallel `Agent` calls with `isolation: "worktree"` are enough. Send them in one message, one call per group.

For a larger batch, a `Workflow` script that pipelines a remediate stage (`isolation: 'worktree'`) into a review stage (no isolation) lets review start on an early-finishing PR while later groups are still being fixed, instead of waiting on the slowest one. Invoking this skill is itself the opt-in to that orchestration, so you don't need to ask again if the batch size warrants it. Either mechanism uses the same prompts and JSON shapes as steps 2 and 3.

## Guardrails recap

Before reporting done, confirm each of these:

- Every issue in the batch is accounted for: fixed with a PR, or flagged with a reason.
- Every PR opened is green, checkstyle included.
- Every correction made after review has a comment on the PR explaining it.
- Every sibling defect found in review is a comment, not part of the diff.
- Every worktree this run created is gone, and no other worktree was touched.
- Nothing was merged, force-pushed, or pushed to `master`.
- Nothing committed or posted mentions private tooling, or how this was orchestrated.
