---
name: accessibility-report
description: Produce or update Gephi's Accessibility Conformance Report (ACR, ITI VPAT 2.5 WCAG edition) for a desktop release. Measures the official release bundle with a Java agent loaded into Gephi (accessibility tree, keyboard walks with real key events, focus visibility, contrast, clipped text at 200%, menus and shortcuts), scans the UI strings, fills the report rows from those measurements, and prepares the short NVDA check that confirms what only a screen reader can tell. Writes under reports/accessibility/<version>/; never publishes, commits or opens PRs unless asked. Use when asked to measure, re-measure, draft, update or finalize the VPAT/ACR/accessibility report for a Gephi version, or to prepare its manual accessibility test session.
---

## Purpose

Universities and other buyers need an Accessibility Conformance Report for Gephi (gephi/gephi.github.io#69). This
skill makes it repeatable per release: every number in the report comes from a measurement of the released
application, and each release only needs a rerun, a review of what changed, and a short manual session when the UI
changed.

## Hard rules

- Measure the **official release bundle** (`measure.sh <version>` downloads it), not a dev build: the report
  describes what users install.
- `measure.sh` puts Gephi in front for ~3 minutes per variant and moves keyboard focus. **Tell the user before
  running it** and ask them to leave the computer idle. If another app takes the focus, walks are marked
  interrupted, the agent stops after three, and `generate.py` refuses walk-based facts: rerun later rather than
  working around it. Never run it in the background while doing other things that could steal focus.
- Never claim **Supports** without evidence (a measurement, an inventory someone judged, or a reasoning row that
  follows from what Gephi is). When unsure, Partially Supports with a precise remark beats an optimistic row.
- Keep ITI's conformance terms exactly: Supports, Partially Supports, Does Not Support, Not Applicable (Not
  Evaluated only for AAA). "(provisional)" is a draft mark that `--final` removes; never publish a provisional row.
- Read-only towards GitHub and the website: no commits, PRs or issue comments unless the user asks.
- `reports/` is untracked scratch output; don't commit it.

## Files

- `agent/src/org/gephi/a11yaudit/`: the Java agent (JDK only; Gephi and NetBeans APIs by reflection, so one jar
  audits any release). `AuditAgent` is the scenario; `Tree` the accessibility tree snapshot and per-node checks;
  `Focus` the Tab walks and focus-indicator comparison; `Menus`, `Keys`, `Env` the rest.
- `scripts/build-agent.sh`: compiles the jar (needs a JDK 17+, finds one or use `A11Y_JDK`).
- `scripts/measure.sh <version> [--variants default,fontsize] [--dist <app>]`: downloads the release (macOS dmg or
  Linux tarball via `gh`), runs Gephi with the agent in a fresh userdir, then `summarize.py`. On macOS Gephi is
  launched through `open` so it gets keyboard focus. On Windows (Git Bash, e.g. Claude Code on Windows) install
  Gephi and pass `--dist "C:/Program Files/Gephi-<version>"`; results go to `measurements/windows`.
- `scripts/summarize.py <measurements dir>`: per-criterion metrics → `summary.json`, findings → `summary.md`.
- `scripts/scan_strings.py <repo> v<version> <out.json>`: English UI strings mentioning position, shape, colour,
  sound or pointer gestures, read from the git tag (1.3.3, 2.5.x evidence).
- `scripts/generate.py <report dir> [--final]`: fills `data/rows.toml` with facts and writes `acr.pdf` (tagged
  PDF printed from `acr.html` by headless Chrome/Edge/Chromium, or `A11Y_BROWSER`), `acr.md`, `facts.json`,
  `review.md`, `manual-checklist.md` (shipped in the kit as `checklist.md`; never write `checklist.md` in the
  report folder, that's the filled copy the tester returns).
- `scripts/make-windows-kit.sh <version>`: zips what the Windows PC needs (agent jar,
  `windows/measure-windows.ps1`, sample files, `checklist.md`, README) into
  `reports/accessibility/<version>/gephi-a11y-windows-kit.zip`. No Git, Python or JDK needed on the PC.
- `scripts/import_manual.py <filled checklist.md> <report dir>`: reads the tester's filled checklist into
  `manual.json`.
- `data/rows.toml`: the 56 WCAG 2.2 A/AA rows: conformance, basis, manual checks, remark templates.
- `data/checklist.toml`: the manual session, kept to what only a screen reader can tell (7 NVDA checks, ~20
  minutes). Keyboard operation is measured by the agent (`Checks.java`): leaving the graph view and shortcuts in
  it (real key presses with java.awt.Robot on Windows, `a11yaudit.robot`), Data Laboratory popup menus, adding a
  filter with Enter, the Appearance colour swatch. The user wants as little manual time as possible: automate
  before adding a manual check.
- `data/import-issues.gexf`: a graph that triggers import issues. The sample graph is the Welcome screen's Les
  Miserables, read from `modules/WelcomeScreen` in this checkout.

## Steps for a release

1. **Measure** (ask the user first, see hard rules):
   `.claude/skills/accessibility-report/scripts/measure.sh <version>` from the repo root, then
   `python3 .claude/skills/accessibility-report/scripts/scan_strings.py . v<version> reports/accessibility/<version>/strings.json`.
   Check `measurements/summary.md`: no failed steps, `foreground` true, no interrupted walks. A failed step usually
   means a menu label, panel name or reflection target changed in that release: fix `AuditAgent`/`Gephi` and rerun.
2. **Windows kit** (Windows-only facts: menu mnemonics, Access Bridge files, window-system buttons): after step 3,
   `make-windows-kit.sh <version>` and give the zip to the user. They run `measure-windows.ps1` (NVDA closed,
   hands off for ~3 minutes) and send back `windows-measurements.zip`. Ask them to put it in the repo's
   `reports/accessibility/<version>/` (macOS blocks this session from reading ~/Downloads), then
   `import_windows.py <zip> reports/accessibility/<version>` (extracts to `measurements/windows/` and
   summarizes; it warns when the PC ran an old kit, which happened once: tell the user to delete the old kit
   folder before unzipping the new one). `generate.py` merges Windows findings with macOS ones; compare the variants in `summary.md`.
   The Windows Graph walk ends `lost` by design: Tab moves focus into the native NEWT window and Java then has
   no focus owner, so manual checks K3 and K4 decide 2.1.2 and the shortcuts there.
3. **Generate** the draft: `python3 .claude/skills/accessibility-report/scripts/generate.py reports/accessibility/<version>`.
4. **Review** every row in `review.md`. Compare `facts.json` with the previous release's: for each changed fact,
   re-decide the row's conformance and adjust its remark template in `rows.toml` (keep templates generic: facts,
   not hard-coded numbers). Look at the screenshots and `focus/*-focused.png`/`*-unfocused.png` evidence before
   trusting a "focus not visible" or contrast finding. Known measurement limits: the OpenGL graph view isn't in
   the painted images (its focus can't be measured; it has no indicator by design); synthetic Ctrl+Tab doesn't
   reproduce the NetBeans window switcher reliably; on macOS menu mnemonics aren't shown.
5. **Screen reader check** when the UI changed noticeably (else reuse the last one and say so in Evaluation
   Methods): the kit's `checklist.md` (about 20 minutes on Windows with NVDA), filled in Notepad as they go. Read it back
   with `import_manual.py <filled checklist.md> reports/accessibility/<version>`, which writes `manual.json`.
   Testers new to NVDA write free text and miss things: read `nvda-speech.txt` too (it showed table cells read
   without headers and property sheet rows read as Java object strings, where the tester only wrote "fail"),
   and write `manual.json` by hand when needed. Give each check a `report` sentence: `generate.py` appends the
   sentences to the rows that list the check ("With the NVDA screen reader: ..."). Update conformance in
   `rows.toml` from the results.
6. **Writing review** before finalizing. The readers are university accessibility and procurement staff, not
   developers. Re-read every row against ITI's definitions ("Supports" means no known defects: a remark that lists
   a defect needs Partially Supports). Update the "Main known barriers" summary in `rows.toml` [report] by user
   group, and the platform note. Describe controls in plain words: add `[names]` entries ("Class@Panel or dialog
   title") for new unnamed controls and `[dialogs]` entries for unclear dialog titles. Avoid jargon (no class
   names except one quoted example, no "agent"), use American spelling, and claim only what was measured or tested;
   say what wasn't tested. A fresh reader agent briefed as an accessibility officer catches what the author misses
   (it found the 2.5.3 rating, missing summary and platform coverage for 0.11.4).
7. **Finalize**: run `generate.py --final` (fails while anything is pending) and show the user `acr.pdf`. The PDF
   is what gets published on gephi.org (a file in gephi/gephi.github.io `public/`, linked from a page); the user
   decides when, and no webpage version is wanted.

## Measurement notes

- NetBeans panels are focus cycle roots: Tab cycles within a panel; panels are switched with Ctrl+Tab (window
  switcher) or the Ctrl+Shift+letter shortcuts. A walk outcome `single` (one focusable control) or `consumed`
  (tables, property sheets and text areas use Tab; Ctrl+Tab leaves) is not a keyboard trap.
- macOS puts Options and About in the application menu: the agent opens them by action ID.
- Statistics and Attributes are tabs behind Filters and Appearance: the agent selects each tab to include it.
- Data Laboratory command buttons are Flamingo components: `Ui.click` handles them.
- The fontsize variant (`--fontsize 28`, 200%) skips keyboard walks; it is for clipped and unscaled text.
