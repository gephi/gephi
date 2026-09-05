---
name: update-dev-links
version: 1.0.0
description: Check the latest run of Gephi's "release" GitHub Actions workflow (gephi/gephi, .github/workflows/release.yml) and, only if it completed fully successfully, refresh the "Development builds" section of README.md with the current Maven Central snapshot download links (Windows/macOS/Linux, x64/aarch64), then open a PR (never merge). Skips silently-safe if the latest run failed, is still in progress, or was a real (non-SNAPSHOT) release rather than a dev build, or if the links are already up to date. Use when asked to update/refresh/sync the README's dev build links, or to check on the latest Gephi dev release.
---

## Purpose

The `release` workflow (triggered by every push to the active dev branch, e.g. `0.11.3`) builds installers/archives for 5 platforms and deploys them straight to the Maven Central snapshots repo. `README.md`'s "Development builds" section hardcodes the download links to the most recent of these, and nobody updates it by hand reliably. This skill re-derives the correct links from first principles (not by scraping the run's UI) and only touches the README when the whole workflow run actually succeeded.

## Hard rules — read first

- **Never write anything unless the triggering run's overall `conclusion` is `success`.** `bundle` is a 5-way matrix with `fail-fast: false`, so a partial failure (e.g. only `windows-x64` failed) still lets 4 platforms deploy — GitHub still reports the run conclusion as `failure` in that case, which is exactly the signal to use. Don't infer success from individual artifacts existing on Central; check the run.
- **Never touch git state in the user's actual working directory.** Do all edits in a throwaway `git worktree` off `origin/master`, exactly like the `sentry-remediate` skill does, so this never collides with in-progress work in the main checkout.
- **Never merge, and never auto-close a stale PR this skill opened earlier.** Open the PR and stop. If an earlier PR from a previous run of this skill is still open, say so in the final report and let the user decide (close, merge, or let it become stale) — closing PRs is a separate action from the one this skill exists to do.
- **Cross-check artifact freshness, don't just trust the run's conclusion.** After building the 5 candidate links, confirm each one's Maven snapshot `<updated>` timestamp actually falls inside the triggering run's `[createdAt, updatedAt]` window (± a few minutes of buffer). A green run conclusion plus a stale artifact timestamp means something inconsistent (e.g. Central hadn't finished indexing, or a concurrent push raced this one) — treat that as "don't update, report why," not as a reason to force it through.
- **If the detected version is not a `-SNAPSHOT`, there's nothing to do.** The per-platform "Output download link" step in the workflow only runs for snapshots; a real release doesn't produce dev links. Report this plainly rather than treating it as an error.
- **This is a real, public, shared repository.** Every push and PR is visible to maintainers immediately — no internal tooling references in the commit message or PR body.

## Inputs

- Repo: `gephi/gephi` via `gh` CLI (already authenticated).
- Workflow: `.github/workflows/release.yml`, workflow name `release`.
- README section to update: `## Latest releases` → `### Development builds` in `README.md` (currently lines ~34-46) — 5 bullet links plus the "Current version is X" sentence. Preserve the exact platform order already there: `windows-x64` (.exe, "Windows"), `macos-x64` (.dmg, "Mac OS X"), `macos-aarch64` (.dmg, "Mac OS X Silicon"), `linux-aarch64` (.tar.gz, "Linux aarch64"), `linux-x64` (.tar.gz, "Linux").
- PR conventions: `.github/pull_request_template.md` + `CONTRIBUTING.md`'s "PR format" section — title `DESCRIPTIVE_SUMMARY` (no issue number, there isn't one), fill Description, tick the Checklist's "Merged with master beforehand", tick **Added tests? no** with reason "docs-only change", tick **Added to documentation? README.md yes**. Label the PR `Documentation`.

## Step-by-step process

### 1. Find the latest `release` run

```bash
gh run list -R gephi/gephi --workflow=release.yml --limit 5 \
  --json databaseId,status,conclusion,headBranch,headSha,createdAt,updatedAt,displayTitle,url
```

Take the most recent entry (first in the list).

- If `status != "completed"`: report "latest run is still in progress" and stop. Do not wait around polling — just report.
- If `status == "completed"` and `conclusion != "success"`: stop. Pull the failing job(s) for the report with `gh run view <databaseId> -R gephi/gephi --json jobs -q '.jobs[] | select(.conclusion=="failure") | .name'` and summarize which platform(s)/stage failed. Do not modify README.md.
- If `conclusion == "success"`: continue to step 2 with this run's `databaseId`, `headSha`, `createdAt`, `updatedAt`, `url`.

### 2. Detect the version that run built

Fetch `pom.xml` as it existed at that commit (no local checkout needed — the repo is public):

```bash
curl -sf "https://raw.githubusercontent.com/gephi/gephi/<headSha>/pom.xml" \
  | grep -A1 '<artifactId>gephi-parent</artifactId>' | grep '<version>' | head -1 \
  | sed -E 's|.*<version>([^<]+)</version>.*|\1|'
```

This mirrors the workflow's own "Detect version" step exactly, so it can't disagree with what the workflow itself deployed under.

- If the result does **not** contain `SNAPSHOT`: this run built a real release, not a dev build. Report "latest successful release run (`<url>`) built `<version>`, a release build — no dev links to update" and stop.
- Otherwise, continue with `VERSION=<version>` (e.g. `0.11.3-SNAPSHOT`).

### 3. Pull per-platform artifact info from Maven Central snapshot metadata

```bash
curl -sf "https://central.sonatype.com/repository/maven-snapshots/org/gephi/gephi/$VERSION/maven-metadata.xml"
```

Do **not** use the top-level `<versioning><snapshot><timestamp>/<buildNumber></snapshot>` — that only reflects whichever artifact deployed *last*, and applying it to every classifier reproduces the exact bug of mixing up unrelated files. Instead, parse the per-classifier `<snapshotVersions><snapshotVersion>` entries, each of which carries its own `<classifier>`, `<extension>`, `<value>` (the real `<version>-<timestamp>-<buildNumber>` for that specific file), and `<updated>` (its deploy timestamp, `YYYYMMDDHHMMSS` UTC).

A small inline script keeps this exact and avoids sed/awk fragility:

```bash
python3 - "$VERSION" <<'EOF'
import sys, urllib.request, xml.etree.ElementTree as ET

version = sys.argv[1]
url = f"https://central.sonatype.com/repository/maven-snapshots/org/gephi/gephi/{version}/maven-metadata.xml"
xml = urllib.request.urlopen(url).read()
root = ET.fromstring(xml)

# (classifier, extension) -> (label, position in README)
WANTED = {
    ("windows-x64", "exe"):      "Windows",
    ("macos-x64", "dmg"):        "Mac OS X",
    ("macos-aarch64", "dmg"):    "Mac OS X Silicon",
    ("linux-aarch64", "tar.gz"): "Linux aarch64",
    ("linux-x64", "tar.gz"):     "Linux",
}

found = {}
for sv in root.iter("snapshotVersion"):
    classifier = sv.findtext("classifier")
    extension = sv.findtext("extension")
    key = (classifier, extension)
    if key in WANTED:
        found[key] = {
            "value": sv.findtext("value"),
            "updated": sv.findtext("updated"),
        }

for key, label in WANTED.items():
    if key not in found:
        print(f"MISSING\t{key[0]}\t{key[1]}")
    else:
        v = found[key]
        classifier, ext = key
        filename = f"gephi-{v['value']}-{classifier}.{ext}"
        print(f"OK\t{classifier}\t{ext}\t{filename}\t{v['updated']}\t{label}")
EOF
```

If any line comes back `MISSING`, stop and report which platform(s) are missing from Central's metadata (this shouldn't happen if the run conclusion was success — treat it as a real inconsistency worth surfacing, not something to paper over).

### 4. Validate freshness against the triggering run

For every `OK` line, confirm its `updated` timestamp (`YYYYMMDDHHMMSS`, UTC) falls within the run's `[createdAt, updatedAt]` window from step 1, with a few minutes of slack on both ends (builds can finish deploying slightly after the job reports "completed", and clocks aren't perfectly aligned). Parse both sides as UTC and compare as epoch seconds — don't shell out to `date -d`/`date -j` for this, their flags differ between macOS and Linux; do it in the same Python process as step 3 (parse `createdAt`/`updatedAt` with `datetime.fromisoformat(x.replace("Z","+00:00"))`, and `updated` with `datetime.strptime(x, "%Y%m%d%H%M%S")` treated as UTC).

If any artifact's `updated` predates the run's `createdAt` by more than the slack window, that platform's file is a stale leftover, not something this run actually deployed — stop and report the mismatch (which classifier, its timestamp, vs. the run window) instead of writing a README that mixes builds from two different runs.

### 5. Build the full download URLs and diff against README

```
https://central.sonatype.com/repository/maven-snapshots/org/gephi/gephi/<VERSION>/<filename>
```

Verify each with a HEAD request before trusting it: `curl -sfI <url> >/dev/null` (non-zero exit = not actually there yet — stop and report rather than writing a dead link).

Read the current `### Development builds` section of `README.md`. If the "Current version is X" string and all 5 links already match exactly what step 3 produced, there's nothing to do — report "README already up to date with `<version>` (run `<url>`)" and stop here. No branch, no PR.

### 6. Apply the edit in an isolated worktree, commit, and open a PR

Run these from the `gephi/gephi` checkout you're already in (find it with `git rev-parse --show-toplevel` if unsure — don't hardcode a path):

```bash
REPO=$(git rev-parse --show-toplevel)
git -C "$REPO" fetch origin
WORKTREE=$(mktemp -d)
git -C "$REPO" worktree add "$WORKTREE" origin/master -b chore/update-dev-links-<version-no-snapshot>
```

In `$WORKTREE/README.md`, update just the `### Development builds` block: the "Current version is X" sentence and the 5 bullet links, in the existing order and format, nothing else in the file.

```bash
cd "$WORKTREE"
git add README.md
git commit -m "$(cat <<'EOF'
Update development build links to <version>

Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>
EOF
)"
git push -u origin chore/update-dev-links-<version-no-snapshot>
```

Before opening the PR, check there isn't already an open one from a prior run of this skill:

```bash
gh pr list -R gephi/gephi --state open --json title,url,headRefName --search "chore/update-dev-links in:head"
```

If one exists and its branch already points at the same version/links, don't open a duplicate — report its URL instead. Otherwise:

```bash
gh pr create -R gephi/gephi \
  --title "Update development build links to <version>" \
  --label "Documentation" \
  --body "$(cat <<'EOF'
## Description

Refreshes the README's "Development builds" links to the artifacts produced by the latest fully-successful `release` workflow run: <run-url>.

## Checklist

- [x] Merged with master beforehand

## Added tests?

- [ ] 👍 yes
- [x] 🙅 no, because they aren't needed

## Added to documentation?
- [x] 👍 README.md
- [ ] 👍 [API Changes](https://github.com/gephi/gephi/blob/master/src/main/javadoc/overview.html)
- [ ] 👍 Additional documentation in [docs](https://github.com/gephi/gephi-documentation)
- [ ] 👍 Relevant code documentation
- [ ] 🙅 no, because they aren't needed
EOF
)"
```

Then clean up the worktree: `git -C "$REPO" worktree remove "$WORKTREE"`.

**Stop here. Never merge the PR.**

### 7. Final report to the user

Short summary only:
- Which run was checked (URL, conclusion) and the version it built.
- If skipped: the specific reason (in progress / failed / release not snapshot / links already current / freshness mismatch), with enough detail (failing job names, or the stale classifier) to act on.
- If a PR was opened: its URL, and the 5 new links it proposes.
- If a duplicate PR already existed: its URL, and that no new PR was opened.

## Guardrails recap

- Only ever acts on a run whose `conclusion` is `success` — a partial matrix failure is a `failure` at the run level, which this skill treats as "do nothing."
- Re-derives links from Maven Central's per-classifier snapshot metadata, never from the workflow's own step summary or by guessing a shared timestamp across platforms.
- Cross-checks artifact freshness against the run's own time window before trusting the metadata.
- All git writes happen in a disposable worktree, never in the user's active checkout.
- Opens a PR and stops — never merges, never auto-closes a previous PR of its own.
