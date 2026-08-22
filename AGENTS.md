# AGENTS.md

Instructions for AI coding agents (Claude Code, Cursor, Codex, etc.) working in this repository.

## Project overview

Gephi is an open-source graph visualization platform built on the Apache NetBeans Platform. The
codebase is a multi-module Maven project (`modules/*`) written in Java, with a Swing/OpenGL desktop
UI. See `README.md` for the product-level description and `pom.xml` for the module list.

## Build and test

Requires JDK 17.

```bash
# Full build + tests
mvn -T 4 clean install

# Faster iteration: skip tests
mvn -T 4 clean install -P skipTests

# Run the app after building
cd modules/application
mvn nbm:cluster-app nbm:run-platform

# Match CI exactly (build + tests + checkstyle)
mvn -T 4 --batch-mode -Djava.awt.headless=true verify -P enableCheckStyle
```

Checkstyle is disabled by default locally and only enforced via the `enableCheckStyle` profile (as
in CI); rules live in `checkstyle.xml`, suppressions in `checkstyle-suppressions.xml`.

## Code style

- Follow the existing style of the file you're editing over any external convention.
- Respect `checkstyle.xml` — run the `enableCheckStyle` profile before proposing changes to
  widely-shared/core modules if you're unsure.
- Gephi is dual-licensed CDDL 1.0 / GPLv3. New source files should carry the standard license
  header used elsewhere in the module (copy from a neighboring file in the same module).

## Repo layout notes

- `modules/` — the actual product code, one Maven module per feature area (e.g. `VisualizationImpl`,
  `GraphModel`, `Layout`, `Filters`, `Export`, `Import*`, `Desktop*`).
- `modules/application` — the assembled desktop app (NetBeans platform cluster).
- Plugin development is a separate concern from core module changes — see the "Create Plug-ins"
  section of `README.md` before assuming a change belongs in `modules/`.

## PR / commit guidelines

- Keep commits scoped to one logical change; this repo takes many small dependency-bump and
  fix PRs, so noisy unrelated diffs stand out.
- Reference the GitHub issue number when a change fixes a reported bug.
- Don't add tests-skipping flags or checkstyle suppressions to get a build green — fix the
  underlying issue.

## Security

- Never commit secrets, API keys, or tokens.
- This is a public repository — do not add personal or employer-internal tooling references
  (private plugin marketplaces, internal service URLs, machine-specific paths) to any file
  intended to be shared (this file, `.claude/CLAUDE.md`, committed skills). Keep that kind of
  config local (gitignored).
