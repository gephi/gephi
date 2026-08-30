# AGENTS.md

Instructions for AI coding agents (Claude Code, Cursor, Codex, etc.) working in this repository.

## Project overview

Gephi is an open-source graph visualization platform built on the Apache NetBeans Platform. The
codebase is a multi-module Maven project (`modules/*`) written in Java, with a Swing/OpenGL desktop
UI. See `README.md` for the product-level description and `pom.xml` for the module list.

For anything beyond a small fix, read these first:

- `ARCHITECTURE.md` — module layout, API/SPI/Lookup conventions, controllers vs. models.
- `CONTRIBUTING.md` — dev environment setup, the exact `mvn` build/test commands, the API-changelog
  convention, branch/PR workflow, release process.

## Build and test

Requires JDK 17. See `CONTRIBUTING.md`'s "Building and running" section for the `mvn` commands
(full build, skip-tests, running the app, matching CI) rather than this file — don't let the two
drift out of sync.

Checkstyle is disabled by default locally and only enforced via the `enableCheckStyle` profile (as
in CI); rules live in `checkstyle.xml`, suppressions in `checkstyle-suppressions.xml`.

## Code style

- Follow the existing style of the file you're editing over any external convention.
- Respect `checkstyle.xml` — run the `enableCheckStyle` profile before proposing changes to
  widely-shared/core modules if you're unsure.
- Gephi is dual-licensed CDDL 1.0 / GPLv3. New source files should carry the standard license
  header used elsewhere in the module (copy from a neighboring file in the same module).

## Repo layout

See `ARCHITECTURE.md` for module structure, naming, and package conventions rather than this file.
One practical note not covered there: plugin development is a separate concern from core module
changes — see the "Create Plug-ins" section of `README.md` before assuming a change belongs in
`modules/`.

## PR / commit guidelines

Follow `CONTRIBUTING.md`'s "PR format" section (title convention, `.github/pull_request_template.md`
checklist) rather than duplicating it here. Two things worth restating for an agent specifically:

- Keep commits scoped to one logical change; this repo takes many small dependency-bump and
  fix PRs, so noisy unrelated diffs stand out.
- Don't add tests-skipping flags or checkstyle suppressions to get a build green — fix the
  underlying issue.

## Security

- Never commit secrets, API keys, or tokens.
- This is a public repository — do not add personal or employer-internal tooling references
  (private plugin marketplaces, internal service URLs, machine-specific paths) to any file
  intended to be shared (this file, `.claude/CLAUDE.md`, committed skills). Keep that kind of
  config local (gitignored).
