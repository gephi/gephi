#!/usr/bin/env python3
"""Builds the Accessibility Conformance Report from the rows, the measurements and the manual results.

Usage: generate.py <report dir> [--final]

<report dir> is reports/accessibility/<version>, holding measurements/summary.json (measure.sh), strings.json
(scan_strings.py) and, once the manual session is done, manual.json. Writes there:
  acr.pdf         the report in the ITI VPAT 2.5 WCAG layout, tagged PDF, the file published on gephi.org
  acr.html        its source, printed by a Chromium browser (Chrome, Edge, Chromium; or A11Y_BROWSER)
  acr.md          Markdown version
  facts.json      every value the remarks use, to diff against the previous release
  review.md       per row: conformance, basis, pending manual checks, the filled remarks
  manual-checklist.md  the manual session for this version (the Windows kit ships it as checklist.md)
With --final, refuses to write if a manual check is unanswered or the contact is missing, and drops draft marks.
Standard library only (Python 3.11+ for tomllib).
"""
import html
import json
import os
import re
import shutil
import statistics
import string
import subprocess
import sys
import tomllib
from datetime import date
from pathlib import Path

SKILL = Path(__file__).resolve().parent.parent
LEVEL_TITLES = {"A": "Table 1: Success Criteria, Level A", "AA": "Table 2: Success Criteria, Level AA"}
ROLE_WORDS = {"push button": "button", "toggle button": "toggle button", "text": "text area", "tree": "tree",
              "table": "table", "combo box": "drop-down list", "check box": "check box",
              "radio button": "radio button", "panel": "panel", "label": "label", "hyperlink": "link"}
# Gephi components whose accessibility role says little, by class name
CLASS_WORDS = {"ColorChooser": "color swatch", "JXHyperlink": "link"}
NONTEXT_NAMES = {
    "Component.focusColor": "the focus ring",
    "Component.focusedBorderColor": "the border of a focused field",
    "Button.focusedBorderColor": "the border of a focused button",
    "Component.borderColor": "field and button borders",
    "Button.borderColor": "field and button borders",
    "CheckBox.icon.borderColor": "check box borders",
    "CheckBox.icon.selectedBackground": "the background of a checked check box",
    "Slider.trackColor": "slider tracks",
    "Slider.thumbColor": "slider thumbs",
    "ToggleButton.toolbar.selectedBackground": "the highlight of the selected toolbar button (e.g. the active graph tool)",
}


def join(items, empty="none"):
    items = [i for i in items if i]
    if not items:
        return empty
    if len(items) == 1:
        return items[0]
    return ", ".join(items[:-1]) + " and " + items[-1]


def uniq(items):
    seen = []
    for i in items:
        if i not in seen:
            seen.append(i)
    return seen


def color_word(hex_color):
    """A plain color name for a #rrggbb value, e.g. 'light gray', 'blue'."""
    r, g, b = (int(hex_color[i:i + 2], 16) / 255 for i in (1, 3, 5))
    hi, lo = max(r, g, b), min(r, g, b)
    if hi - lo < 0.12:
        return ("white" if hi > 0.97 else "very light gray" if hi > 0.9 else "light gray" if hi > 0.7
                else "gray" if hi > 0.4 else "dark gray" if hi > 0.15 else "black")
    if hi == r:
        hue = ((g - b) / (hi - lo)) % 6
    elif hi == g:
        hue = (b - r) / (hi - lo) + 2
    else:
        hue = (r - g) / (hi - lo) + 4
    name = ["red", "orange", "yellow", "green", "green", "cyan", "blue", "blue", "purple", "purple", "pink",
            "red"][int(hue * 2) % 12]
    return ("light " if hi > 0.85 and lo > 0.5 else "") + name


def colors(fg, bg):
    return f"{color_word(fg)} on {color_word(bg)}"


def role_word(item):
    if item.get("class") in CLASS_WORDS:
        return CLASS_WORDS[item["class"]]
    role = item.get("role")
    if role == "text":
        return "text field" if "Field" in item.get("class", "") else "text area"
    return ROLE_WORDS.get(role, role or "control")


class Namer:
    """Plain descriptions of controls: names from rows.toml [names] ("Class@Panel or dialog title") for controls
    without an accessible name, dialog names from [dialogs], else 'the "Run" button in the Layout panel'."""

    def __init__(self, names, dialogs):
        self.names = names
        self.dialogs = dialogs

    def place(self, item, dialog):
        if item.get("panel"):
            return f'the {item["panel"]} panel'
        if dialog:
            return f"the {self.dialogs.get(dialog, dialog)} dialog"
        return None

    def part(self, item, dialog=None, prefer_text=False):
        first, second = ("text", "name") if prefer_text else ("name", "text")
        label = item.get(first) or item.get(second)
        where = item.get("panel") or dialog
        key = f'{item.get("class")}@{where}'
        if not item.get("name") and key in self.names:
            return self.names[key]
        role = role_word(item)
        place = self.place(item, dialog)
        suffix = f" in {place}" if place else ""
        if label:
            return f'the "{label}" {role}{suffix}'
        if item.get("tooltip"):
            return f'an unnamed {role} (tooltip "{item["tooltip"]}"){suffix}'
        return f"an unnamed {role}{suffix}"


def counted(phrases):
    """Identical descriptions once, with a count for generic ones: 2 x 'an unnamed tree in X' -> '2 unnamed trees
    in X'."""
    out = []
    for p in uniq(phrases):
        n = phrases.count(p)
        m = re.match(r"an unnamed (\w+(?: \w+)?)( .*)?$", p)
        if n > 1 and m:
            out.append(f"{n} unnamed {m.group(1)}s{m.group(2) or ''}")
        else:
            out.append(p)
    return out


def window_title(scene):
    return (scene.get("windows") or [None])[0] or ""


def collect(variant, key, kind=None):
    """Items of one list across scenes, with the title of the dialog they are in (None in panels). With a list of
    variants (macOS and Windows runs), their items are merged."""
    if isinstance(variant, list):
        # macOS items, then the Windows items it doesn't have (sizes and fonts differ slightly between platforms)
        out, seen = [], set()
        for v in variant:
            if not v:
                continue
            new = [(x, w) for x, w in collect(v, key, kind) if (x.get("label"), w) not in seen]
            out += new
            seen |= {(x.get("label"), w) for x, w in new}
        return out
    out = []
    titles = set()
    for name, s in variant["scenes"].items():
        # The startup scene (before a graph is open) repeats panels of the Overview scene
        if kind and s["kind"] != kind or s["kind"] == "startup":
            continue
        # A dialog measured twice (the import report with and without issues) counts once
        if s["kind"] == "dialog":
            if window_title(s) in titles:
                continue
            titles.add(window_title(s))
        for x in s.get(key) or []:
            out.append((x, window_title(s) if s["kind"] == "dialog" else None))
    return out


def facts(summary, strings, manual, checklist, names):
    d = summary["variants"]["default"]
    fs = summary["variants"].get("fontsize")
    win = summary["variants"].get("windows")
    rt = d["runtime"]
    sc = d["scenes"]
    nm = names
    f = {}
    f["version"] = re.search(r"Gephi (\S+)", rt["title"]).group(1)
    os_name = {"Mac OS X": "macOS"}.get(rt["os.name"], rt["os.name"])
    arch = {"aarch64": "Apple silicon" if os_name == "macOS" else "ARM64", "x86_64": "x64", "amd64": "x64"}.get(
        rt["os.arch"], rt["os.arch"])
    f["platform"] = f"{os_name} {rt['os.version']} on {arch}"
    f["java"] = f"{rt['java.vendor']} {rt['java.runtime.version'].split('+')[0]}"
    both = [d, win] if win else d
    if win:
        wr = win["runtime"]
        f["platform"] += f", and {wr['os.name']} on x64"
        win_java = wr["java.runtime.version"].split("+")[0]
        if win_java != rt["java.runtime.version"].split("+")[0]:
            f["java"] += f", {win_java} on Windows"
    f["tested_platforms"] = f"{os_name} and Windows" if win else os_name
    f["date"] = d["date"]
    f["laf"] = d["theme"]["lookAndFeel"]
    f["font"] = f"{d['theme']['labelFontSize']:g} pt"

    dialogs = [s for s in sc.values() if s["kind"] == "dialog"]
    titles = uniq(window_title(s) for s in dialogs)
    f["dialog_count"] = len(titles)
    f["dialogs"] = join([nm.dialogs.get(t, t) for t in titles])
    f["titles"] = join(f'"{t}"' for t in titles)
    f["main_title"] = window_title(sc["overview"])
    f["panels"] = join(uniq(p for s in sc.values() if s["kind"] == "perspective" for p in s.get("panels") or []))

    ov = sc["overview"]
    f["overview_interactive"] = ov["interactive"]
    f["overview_unnamed_n"] = len(ov["unnamed"])
    f["overview_unnamed"] = join(counted([nm.part(x) for x in ov["unnamed"]]))
    f["dialogs_unnamed"] = join(counted([nm.part(x, w) for x, w in collect(both, "unnamed", "dialog")]))
    canvas = [c for s in sc.values() for c in s.get("canvas") or []]
    f["canvas_name"] = canvas[0]["name"] if canvas else "(not found)"
    datalab = next((s for s in sc.values() if s["kind"] == "perspective" and "lab" in s.get("perspective", "")
                    .lower()), None) or sc.get("data-laboratory")
    tables = (datalab or {}).get("tables") or []
    f["datalab_headers"] = join([h for h in (tables[0]["headers"] if tables else []) if h][:5])
    group = next((c for s in sc.values() for t in s.get("tables") or [] for row in t.get("sampleCells") or []
                  for c in row if c and "Sheet$Set" in c), None)
    f["property_group_cell"] = (group.split(";")[0] + "; …]") if group else "(not found)"
    f["tabs_note"] = ("; the tabs that switch between panels (e.g. Filters and Statistics) are not exposed as "
                      "individual tabs" if not ov.get("pageTabs") else "")
    walks = {w["scope"]: w for s in sc.values() for w in s.get("walks") or []}
    # Scenes showing the same window (the import report, with and without issues) count once
    titled, seen_titles = [], set()
    for s in sc.values():
        key = window_title(s) if s["kind"] == "dialog" else s["kind"] + s.get("perspective", "")
        if key not in seen_titles:
            seen_titles.add(key)
            titled.append(s)
    f["_interrupted_walks"] = [w["scope"] for w in walks.values() if w["outcome"] in ("lost", "noFocus")]
    f["appearance_order"] = " → ".join(o.get("name") or f'(unnamed {role_word(o)})'
                                       for o in (walks.get("Appearance") or {}).get("order", []))
    stops = []
    for s in titled:
        for w in s.get("walks") or []:
            for x in w.get("unnamedStops") or []:
                if x.get("role") not in ("table", "tree", "text", "scroll pane"):
                    stops.append(nm.part({**x, "name": None}, window_title(s) if s["kind"] == "dialog" else None))
    f["unnamed_tab_stops"] = join(counted(stops))
    hidden = []
    for s in titled:
        for w in s.get("walks") or []:
            hidden += [nm.part(x) for x in w.get("focusHidden") or []]
    f["focus_hidden"] = join(counted(hidden))
    f["string_files"] = (strings or {}).get("files", "the")

    km = d["keymap"]
    f["shortcuts_n"] = km["shortcuts"] if not win else f'{win["keymap"]["shortcuts"]} on Windows, {km["shortcuts"]} on macOS'
    char_keys = km["characterKeys"] + ((win or {}).get("keymap") or {}).get("characterKeys", [])
    f["character_keys_note"] = "" if not char_keys else f"; single-key shortcuts found: {join(uniq(char_keys))}"
    nf = {}
    for x, w in collect(both, "buttonsNotFocusable"):
        nf.setdefault(nm.place(x, w), []).append(f'"{x.get("name") or x.get("text")}"')
    f["not_focusable_n"] = sum(len(v) for v in nf.values())
    f["not_focusable"] = join([f"{join(v)} in {k}" for k, v in nf.items()])
    menus = d["menus"]
    f["menus_without_mnemonic"] = join(menus["topLevelWithoutMnemonic"], "No")
    consumers = [nm.dialogs.get(w["scope"].title(), w["scope"].title()) + " dialog"
                 if w["scope"] in ("plugins", "options") else w["scope"]
                 for w in walks.values() if w["outcome"] == "consumed"]
    consumers += [w["scope"] for w in walks.values() if w["outcome"] == "single" and w.get("stuckAt", "")
                  and "table" in w["stuckAt"]]
    f["tab_consumers"] = join(uniq(consumers))
    f["label_not_in_name"] = join(uniq([
        f'the "{x.get("text")}" {role_word(x)} is named "{x.get("name")}"'
        for x, w in collect(both, "labelNotInName")]))
    f["label_not_in_name_where"] = join(uniq([nm.place(x, w) for x, w in collect(both, "labelNotInName")]))
    f["locale"] = rt.get("frameLocale")
    f["windows_opened_by_tab"] = sum(len(w.get("windowsOpenedByTab") or []) for w in walks.values()) or "no"
    issues = sc.get("import-report-issues", {}).get("tables") or []
    cells = [c for row in (issues[0].get("sampleCells") or []) for c in row if c and len(c) > 25] if issues else []
    f["import_issue"] = cells[0] if cells else "(not found)"
    bridge_files = (win or d)["runtime"].get("accessBridgeFiles") or []
    f["bridge"] = ("it includes the jdk.accessibility module and, on Windows, the Java Access Bridge"
                   if any("accessbridge" in b.lower() for b in bridge_files)
                   else "it includes the jdk.accessibility module")
    jre = ((win or {}).get("runtime") or {}).get("javaHome") or ""
    f["jabswitch_dir"] = jre + "\\bin" if jre else "jre-x64\\<runtime>\\bin in the Gephi folder"

    pairs = {(p["foreground"], p["background"]): p for p in d["theme"]["pairs"]}
    f["label_contrast"] = pairs[("Label.foreground", "Panel.background")]["contrast"]
    f["selection_contrast"] = pairs[("Table.selectionForeground", "Table.selectionBackground")]["contrast"]
    f["tooltip_contrast"] = pairs[("ToolTip.foreground", "ToolTip.background")]["contrast"]
    ph = pairs[("TextField.placeholderForeground", "TextField.background")]
    f["placeholder_contrast"] = ph["contrast"]
    f["placeholder_colors"] = colors(ph["fg"], ph["bg"])
    tcf = []
    for x, w in collect(both, "textContrastFailures"):
        tcf.append(f'{nm.part(x, w, prefer_text=True)} ({colors(x["fg"], x["bg"])}, {x["contrast"]}:1)')
    f["text_contrast_failures"] = join(uniq(tcf))
    nontext = {}
    for p in d["theme"]["failingPairs"]:
        if p["kind"] == "nonText":
            name = NONTEXT_NAMES.get(p["foreground"], p["foreground"])
            if name not in nontext or p["contrast"] < nontext[name]["contrast"]:
                nontext[name] = p
    f["nontext_failures"] = join([f'{n} ({colors(p["fg"], p["bg"])}, {p["contrast"]}:1)'
                                  for n, p in nontext.items()])
    medians = [m for w in walks.values() for m in w.get("focusMedianContrasts") or []]
    f["focus_change_median"] = round(statistics.median(medians), 2) if medians else "(not measured)"
    icons = [x for v in (d, fs) if v for s in v["scenes"].values() for x in s.get("iconContrast") or []]
    if icons:
        low = [x for x in icons if x["contrast"] < 3]
        if low:
            listed = join(uniq([f'{nm.part(x)} ({x["contrast"]}:1)' for x in low]))
            f["icons"] = f"Icons below 3:1: {listed}"
        else:
            lowest = min(x["contrast"] for x in icons)
            f["icons"] = f"The icons of icon-only buttons pass: the lowest measured icon contrast is {lowest}:1"
    else:
        f["icons"] = "Icon contrast was not measured"
    nv = []
    for s in titled:
        for w in s.get("walks") or []:
            for x in w.get("focusNotVisible") or []:
                if x.get("role") != "scroll pane":
                    nv.append(nm.part(x, window_title(s) if s["kind"] == "dialog" else None))
    f["focus_not_visible"] = join(counted(nv))
    f["tooltip_dismiss_s"] = d["tooltips"]["dismissDelayMs"] / 1000
    f["tooltip_dismiss_s"] = int(f["tooltip_dismiss_s"]) if f["tooltip_dismiss_s"].is_integer() \
        else f["tooltip_dismiss_s"]
    if fs:
        small, numbers = [], []
        for x, w in collect(fs, "smallText"):
            place = nm.place(x, w) or "a dialog"
            if re.fullmatch(r"[\d.,\s]+", x["text"]):
                numbers.append(f"the numbers in {place}")
            else:
                small.append(f'"{x["text"]}" ({place})')
        f["small_200"] = join(uniq(small + numbers))
        clipped = [f'"{x.get("text") or x.get("name")}" ({nm.place(x, w) or "a dialog"})'
                   for x, w in collect(fs, "clippedText")]
        cells = [c.rsplit(" (", 1)[0] for s in fs["scenes"].values() for c in s.get("clippedCells") or []]
        cells = [c for c in uniq(cells) if len(c) < 40]
        if cells:
            clipped.append(f"property names such as {join([chr(34) + c + chr(34) for c in cells[:5]])}")
        f["clipped_200"] = join(uniq(clipped))
    else:
        f["small_200"] = f["clipped_200"] = "(not measured)"
    tf = [f'{nm.part(x, w, prefer_text=True)} ({x["size"][0]}×{x["size"][1]} pixels)'
          for x, w in collect(both, "targetSizeFailures")]
    f["targets_fail"] = join(uniq(tf))
    f["links"] = join(uniq([f'"{l}"' for s in sc.values() for l in s.get("links") or [] if l]))
    run = d.get("layoutRun") or {}
    f["layout_status_events"] = run.get("statusOrProgressEvents", "(not measured)")
    if run.get("statusOrProgressEvents") == 0:
        f["layout_status_events"] = "no"

    f.update(keyboard_facts(d, win))

    meta = (manual or {}).get("_session") or {}
    if manual and all(c["id"] in manual for c in checklist["check"]):
        nvda = f'NVDA {meta["nvda"]}' if meta.get("nvda") else "NVDA"
        f["manual_status"] = (f'a screen reader check with {nvda} and the Java Access Bridge on '
                              f'{meta.get("os", "Windows")}, {meta.get("date", "")}: menus, toolbar buttons, the graph '
                              f'view, tables, property sheets, states and status messages.')
        f["screen_reader"] = f"{nvda} on {meta.get('os', 'Windows')}"
    else:
        f["manual_status"] = "not yet done (rows marked provisional are pending it)."
        f["screen_reader"] = "none yet"
    return f


def keyboard_facts(d, win):
    """Sentences from the agent's keyboard operation checks (Checks.java), per platform."""
    f = {}
    mac = (d.get("keyboardChecks") or {})
    wkc = ((win or {}).get("keyboardChecks") or {})
    f["_windows_keyboard_missing"] = not wkc.get("graphView")

    def keys_of(kc):
        real = (kc.get("graphView") or {}).get("robot")
        # Without real key presses, Ctrl+Tab can't drive the NetBeans window switcher: not conclusive
        return {k["key"]: k for k in (kc.get("graphView") or {}).get("keys", [])
                if not k.get("skipped") and (real or k["key"] != "Ctrl+Tab")}, real

    def graph(kc, platform):
        keys, real = keys_of(kc)
        if not keys:
            return None, None
        leaves = [k for k in ("Tab", "Shift+Tab", "Ctrl+Tab") if k in keys and not keys[k]["stillInGraphView"]]
        stays = [k for k in ("Tab", "Shift+Tab", "Ctrl+Tab") if k in keys and keys[k]["stillInGraphView"]]
        shortcuts = [k for k in keys if k.endswith("+2") or k.endswith("+Shift+L")]
        dead = [k for k in shortcuts if not (keys[k]["perspectiveChanged"] or keys[k]["panelChanged"])]
        who = f"On {platform}" + ("" if real else " (tested with simulated key presses)")
        out = f"{who}, " + (f"{join(leaves)} move{'s' if len(leaves) == 1 else ''} focus out of the graph view"
                             if leaves else "no key moves focus out of the graph view")
        if stays:
            out += f" but {join(stays)} {'does' if len(stays) == 1 else 'do'} not"
        if dead and len(dead) == len(shortcuts):
            out += f", and global shortcuts such as {join(dead)} only work after leaving it"
        elif dead:
            out += f", and {join(dead)} only work{'s' if len(dead) == 1 else ''} after leaving it"
        else:
            out += ", and global shortcuts work while it has focus"
        return out, leaves

    sentences, exits = [], {}
    for kc, platform in ((wkc, "Windows"), (mac, "macOS")):
        sentence, leaves = graph(kc, platform)
        if sentence:
            sentences.append(sentence)
            exits[platform] = leaves
    f["graph_keys"] = (". ".join(sentences) + ".") if sentences else "(not measured)."
    if exits:
        tab_everywhere = all("Tab" in v for v in exits.values())
        f["graph_exit"] = ("Tab leaves the graph view" + (f" on {join(list(exits))}" if tab_everywhere else "")
                           if tab_everywhere else "the graph view keeps the focus on "
                           + join([p for p, v in exits.items() if "Tab" not in v]))
    else:
        f["graph_exit"] = "leaving the graph view was not measured"

    # Each check from the platform where it gave a usable result (Swing behaves the same on both)
    popups = next((p for p in (wkc.get("commandPopups"), mac.get("commandPopups"))
                   if p and any("menuDetectedWhenClicked" in x for x in p)), [])
    if popups and not all(x.get("opensWith") or x.get("menuDetectedWhenClicked") for x in popups):
        popups = []  # the check couldn't see an open menu, so "doesn't open" would mean nothing
    works, mouse = [], []
    if popups:
        closed = [p for p in popups if not p.get("opensWith")]
        names = join([f'"{p["button"] or p["name"]}"' for p in closed])
        if closed:
            mouse.append(f"the column actions of the Data Laboratory: {len(closed)} buttons ({names}) take focus but "
                         f"their menus don't open from the keyboard")
        if len(closed) < len(popups):
            works.append("the Data Laboratory buttons whose menus open with Space, Enter or Down")
    fe = next((x for x in (wkc.get("filterEnter"), mac.get("filterEnter")) if x and "addedWithEnter" in x), {})
    if "addedWithEnter" in fe:
        f["filter_enter"] = ("filters are added to a query from the library with Enter instead of dragging"
                             if fe["addedWithEnter"] else
                             "Enter on a filter in the library doesn't add it to a query, so building queries needs "
                             "dragging")
        (works if fe["addedWithEnter"] else mouse).append("adding a filter to a query (Enter in the filter library)")
    else:
        f["filter_enter"] = "adding filters without dragging was not measured"
    sw = next((x for x in (wkc.get("colorSwatch"), mac.get("colorSwatch")) if x and not x.get("error")), {})
    if sw.get("opensPicker"):
        works.append("the color swatches of the Appearance panel (Space opens the color picker)")
    elif sw:
        mouse.append("choosing a color from the Appearance panel's color swatches")
    f["kb_works_extra"] = "".join(f"; {w}" for w in works)
    f["kb_mouse_extra"] = "".join(f"; {m}" for m in mouse)
    return f


class Facts(dict):
    def __missing__(self, key):
        raise KeyError(f"remark uses unknown fact {{{key}}}")


def fill(text, f):
    return string.Formatter().vformat(text, (), Facts(f))


def criterion_label(row):
    scope = row.get("scope")
    suffix = f"Level {row['level']}" + (f" {scope}" if scope else "")
    return f'{row["id"]} {row["name"]} ({suffix})'


def sort_key(row):
    return tuple(int(p) for p in row["id"].split("."))


def build(report_dir, final):
    rows_doc = tomllib.loads((SKILL / "data/rows.toml").read_text(encoding="utf-8"))
    checklist = tomllib.loads((SKILL / "data/checklist.toml").read_text(encoding="utf-8"))
    summary = json.loads((report_dir / "measurements/summary.json").read_text(encoding="utf-8"))
    strings_file = report_dir / "strings.json"
    strings = json.loads(strings_file.read_text(encoding="utf-8")) if strings_file.exists() else None
    manual_file = report_dir / "manual.json"
    manual = json.loads(manual_file.read_text(encoding="utf-8")) if manual_file.exists() else {}
    namer = Namer(rows_doc.get("names", {}), rows_doc.get("dialogs", {}))
    f = facts(summary, strings, manual, checklist, namer)
    report = rows_doc["report"]
    rows = sorted(rows_doc["row"], key=sort_key)
    problems = []
    if f["_windows_keyboard_missing"]:
        problems.append("no Windows keyboard checks yet (rerun the Windows kit)")
    if f["_interrupted_walks"]:
        problems.append(f'keyboard walks without focus, rerun measure.sh: {", ".join(f["_interrupted_walks"])}')
    for row in rows:
        row["filled"] = fill(row["remarks"], f).replace("\n", " ")
        heard = uniq([manual[m]["report"] for m in row.get("manual", []) if (manual.get(m) or {}).get("report")])
        if heard:
            said = "; ".join(heard)
            row["filled"] += f" With the NVDA screen reader: {said}."
        row["pending"] = [m for m in row.get("manual", []) if m not in manual]
        if row.get("windows_keyboard") and f["_windows_keyboard_missing"]:
            row["pending"].append("Windows keyboard run")
        failed = [m for m in row.get("manual", []) if (manual.get(m) or {}).get("result") in ("fail", "partial")]
        row["failedChecks"] = failed
        if row["pending"]:
            problems.append(f'{row["id"]}: manual checks pending {row["pending"]}')
    if report["contact"] == "TODO":
        problems.append("contact information missing (rows.toml [report] contact)")
    if final and problems:
        sys.exit("Not final:\n  " + "\n  ".join(problems))
    header = {
        "title": report["title"],
        "edition": report["edition"],
        "product": f'{report["product"]} {f["version"]} (desktop application for Windows, macOS and Linux)',
        "date": date.today().strftime("%B %Y") if final else f'Draft of {date.today().isoformat()}',
        "description": fill(report["description"], f),
        "contact": report["contact"],
        "notes": [fill(n, f) for n in report["notes"]],
        "summary_intro": fill(report.get("summary_intro", ""), f),
        "summary": [fill(x, f) for x in report.get("summary", [])],
        "methods": [fill(m, f) for m in report["evaluation_methods"]],
    }
    (report_dir / "facts.json").write_text(json.dumps(f, indent=1, ensure_ascii=False), encoding="utf-8")
    (report_dir / "acr.md").write_text(to_markdown(header, rows, final), encoding="utf-8")
    body = to_html_body(header, rows, final)
    (report_dir / "acr.html").write_text(to_html_page(header, body), encoding="utf-8")
    pdf = print_pdf(report_dir / "acr.html", report_dir / "acr.pdf")
    (report_dir / "review.md").write_text(to_review(rows, manual, checklist, f), encoding="utf-8")
    # Not checklist.md: that's the name of the filled-in copy the tester sends back
    (report_dir / "manual-checklist.md").write_text(to_checklist(checklist, f), encoding="utf-8")
    print(f"Wrote {'acr.pdf, ' if pdf else ''}acr.html, acr.md, facts.json, review.md and manual-checklist.md in "
          f"{report_dir}")
    if not pdf:
        print("No PDF: no Chromium browser found (set A11Y_BROWSER to Chrome, Edge or Chromium)")
    if problems:
        print("Draft: " + "; ".join(problems[:3]) + (f" (+{len(problems) - 3} more)" if len(problems) > 3 else ""))


BROWSERS = [
    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
    "/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge",
    "/Applications/Chromium.app/Contents/MacOS/Chromium",
    r"C:\Program Files\Google\Chrome\Application\chrome.exe",
    r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
]


def print_pdf(source, target):
    """Prints the HTML to a tagged PDF (with a document outline) using a headless Chromium browser."""
    candidates = [os.environ.get("A11Y_BROWSER")] + BROWSERS + [
        shutil.which(n) for n in ("google-chrome", "chromium", "chromium-browser", "microsoft-edge")]
    browser = next((b for b in candidates if b and Path(b).exists()), None)
    if not browser:
        return False
    subprocess.run([browser, "--headless=new", "--disable-gpu", "--no-pdf-header-footer", "--export-tagged-pdf",
                    "--generate-pdf-document-outline", f"--print-to-pdf={target}", source.resolve().as_uri()],
                   check=True, capture_output=True, timeout=120)
    return target.exists()


def conformance(row, final):
    level = row["conformance"]
    if not final and row["pending"]:
        level += " (provisional)"
    return level


def to_markdown(h, rows, final):
    out = [f'# {h["title"]}', f'## {h["edition"]}', ""]
    if not final:
        out += ["> **Draft.** Rows marked provisional wait for the manual session.", ""]
    out += [f'**Name of Product/Version:** {h["product"]}', "", f'**Report Date:** {h["date"]}', "",
            f'**Product Description:** {h["description"]}', "", f'**Contact Information:** {h["contact"]}', "",
            "**Notes:**", ""]
    if h["summary"]:
        out += [f'- {h["summary_intro"]}'] + [f"    - {x}" for x in h["summary"]]
    out += [f"- {n}" for n in h["notes"]] + ["", "**Evaluation Methods Used:**", ""]
    out += [f"- {m}" for m in h["methods"]]
    out += ["", "## Applicable Standards/Guidelines", "", "| Standard/Guideline | Included In Report |",
            "|---|---|"]
    for v in ("2.0", "2.1", "2.2"):
        out.append(f"| Web Content Accessibility Guidelines {v} | Level A (Yes), Level AA (Yes), Level AAA (No) |")
    out += ["", "## Terms", ""] + [f"- **{t}**: {d}" for t, d in TERMS] + ["", "## WCAG 2.x Report", "",
                                                                             WCAG_NOTE, ""]
    for level in ("A", "AA"):
        out += [f"### {LEVEL_TITLES[level]}", "", "| Criteria | Conformance Level | Remarks and Explanations |",
                "|---|---|---|"]
        for r in rows:
            if r["level"] == level:
                out.append(f'| {criterion_label(r)} | {conformance(r, final)} | {r["filled"]} |')
        out.append("")
    out += ["### Table 3: Success Criteria, Level AAA", "", "Not evaluated.", "", "## Legal Disclaimer", "",
            DISCLAIMER, ""]
    return "\n".join(out)


TERMS = [
    ("Supports", "The functionality of the product has at least one method that meets the criterion without known "
                 "defects or meets with equivalent facilitation."),
    ("Partially Supports", "Some functionality of the product does not meet the criterion."),
    ("Does Not Support", "The majority of product functionality does not meet the criterion."),
    ("Not Applicable", "The criterion is not relevant to the product."),
    ("Not Evaluated", "The product has not been evaluated against the criterion. This can only be used in WCAG "
                      "Level AAA criteria."),
]
WCAG_NOTE = ("Note: When reporting on conformance with the WCAG 2.x Success Criteria, they are scoped for full "
             "pages, complete processes, and accessibility-supported ways of using technology as documented in "
             "the WCAG 2.0 Conformance Requirements.")
DISCLAIMER = ("This report describes the accessibility of the Gephi desktop application as tested. Gephi is open "
              "source software provided without warranty, as stated in its license. \"Voluntary Product "
              "Accessibility Template\" and \"VPAT\" are registered service marks of the Information Technology "
              "Industry Council (ITI).")


def linkify(text):
    """Escapes text and turns URLs and email addresses into links."""
    out = html.escape(text)
    out = re.sub(r"(https?://[^\s)<]+[^\s).,;:<])", r'<a href="\1">\1</a>', out)
    return re.sub(r"\b([\w.+-]+@[\w-]+\.[\w.]+\w)\b", r'<a href="mailto:\1">\1</a>', out)


def to_html_body(h, rows, final):
    e = linkify
    out = [f'<h1>{e(h["title"])}</h1>', f'<p class="acr-edition">{e(h["edition"])}</p>']
    if not final:
        out.append('<p class="acr-draft"><strong>Draft.</strong> Rows marked provisional wait for the manual '
                   'session.</p>')
    out += [f'<p><strong>Name of Product/Version:</strong> {e(h["product"])}</p>',
            f'<p><strong>Report Date:</strong> {e(h["date"])}</p>',
            f'<p><strong>Product Description:</strong> {e(h["description"])}</p>',
            f'<p><strong>Contact Information:</strong> {e(h["contact"])}</p>',
            "<p><strong>Notes:</strong></p>", "<ul>"]
    if h["summary"]:
        out += [f'<li>{e(h["summary_intro"])}<ul>'] + [f"<li>{e(x)}</li>" for x in h["summary"]] + ["</ul></li>"]
    out += [f"<li>{e(n)}</li>" for n in h["notes"]] + [
        "</ul>", "<p><strong>Evaluation Methods Used:</strong></p>", "<ul>"] + [
        f"<li>{e(m)}</li>" for m in h["methods"]] + ["</ul>"]
    out += ["<h2>Applicable Standards/Guidelines</h2>",
            "<p>This report covers the degree of conformance for the following accessibility standard/guidelines:"
            "</p>", "<table>", "<thead><tr><th scope=\"col\">Standard/Guideline</th>"
                               "<th scope=\"col\">Included In Report</th></tr></thead>", "<tbody>"]
    for v in ("2.0", "2.1", "2.2"):
        out.append(f"<tr><td>Web Content Accessibility Guidelines {v}</td>"
                   "<td>Level A (Yes)<br>Level AA (Yes)<br>Level AAA (No)</td></tr>")
    out += ["</tbody>", "</table>", "<h2>Terms</h2>",
            "<p>The terms used in the Conformance Level information are defined as follows:</p>", "<ul>"]
    out += [f"<li><strong>{e(t)}</strong>: {e(d)}</li>" for t, d in TERMS] + ["</ul>"]
    out += ["<h2>WCAG 2.x Report</h2>", f"<p>{e(WCAG_NOTE)}</p>"]
    for level in ("A", "AA"):
        out += [f"<h3>{e(LEVEL_TITLES[level])}</h3>", "<table>",
                "<thead><tr><th scope=\"col\">Criteria</th><th scope=\"col\">Conformance Level</th>"
                "<th scope=\"col\">Remarks and Explanations</th></tr></thead>", "<tbody>"]
        for r in rows:
            if r["level"] == level:
                out.append(f'<tr id="sc-{r["id"]}"><th scope="row">{e(criterion_label(r))}</th>'
                           f'<td>{e(conformance(r, final))}</td><td>{e(r["filled"])}</td></tr>')
        out += ["</tbody>", "</table>"]
    out += ["<h3>Table 3: Success Criteria, Level AAA</h3>", "<p>Not evaluated.</p>",
            "<h2>Legal Disclaimer</h2>", f"<p>{e(DISCLAIMER)}</p>"]
    return "\n".join(out) + "\n"


def to_html_page(h, body):
    return f"""<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{html.escape(h["title"])}</title>
<style>
  :root {{ color-scheme: light; --text: #1a1a1a; --muted: #555; --line: #c8c8c8; --head: #f0f0f0; --draft: #fff4ce; }}
  body {{ font: 16px/1.5 system-ui, -apple-system, "Segoe UI", sans-serif; color: var(--text); background: #fff;
         max-width: 72rem; margin: 0 auto; padding: 1rem 1rem 3rem; }}
  h1 {{ font-size: 1.8rem; margin-bottom: 0; }}
  .acr-edition {{ color: var(--muted); margin-top: .25rem; }}
  .acr-draft {{ background: var(--draft); padding: .5rem .75rem; border-left: 4px solid #b38600; }}
  table {{ border-collapse: collapse; width: 100%; margin: 1rem 0; }}
  th, td {{ border: 1px solid var(--line); padding: .5rem; vertical-align: top; text-align: left; }}
  thead th {{ background: var(--head); }}
  tbody th {{ font-weight: 600; width: 22%; }}
  tbody td:nth-child(2) {{ width: 16%; }}
  a {{ color: #0b5cad; }}
  @page {{ size: A4; margin: 16mm 14mm 18mm;
    @bottom-right {{ content: "Page " counter(page) " of " counter(pages); font: 9pt system-ui, sans-serif; color: #555; }}
    @bottom-left {{ content: "{html.escape(h["title"])}"; font: 9pt system-ui, sans-serif; color: #555; }} }}
  @media print {{ body {{ max-width: none; font-size: 10pt; padding: 0; }} tr {{ break-inside: avoid; }}
    thead {{ display: table-header-group; }} h2, h3 {{ break-after: avoid; }} }}
</style>
</head>
<body>
<main>
{body}</main>
</body>
</html>
"""


def to_review(rows, manual, checklist, f):
    checks = {c["id"]: c for c in checklist["check"]}
    out = ["# Review", "", "Each row with its basis, the manual checks it waits on and the filled remarks. "
                           "Compare facts.json with the previous release's to spot changed rows.", ""]
    for r in rows:
        out.append(f'## {criterion_label(r)}: {r["conformance"]}')
        out.append(f'basis: {r["basis"]}; manual: '
                   + (", ".join(f'{m} ({(manual.get(m) or {}).get("result", "pending")})'
                                for m in r.get("manual", [])) or "none"))
        for m in r["failedChecks"]:
            out.append(f'- **{m} {checks[m]["title"]}: {manual[m]["result"]}**: {manual[m].get("note", "")}')
        out += ["", r["filled"], ""]
    return "\n".join(out)


def to_checklist(checklist, f):
    """The manual session as a text file the tester fills in (read back by import_manual.py)."""
    s = checklist["session"]
    out = [f'# Manual accessibility session, Gephi {f["version"]}', "",
           "Write your results into this file as you go (Notepad is fine): after each \"Result:\" put pass, "
           "partial, fail or skip, and say what happened after \"Note:\", especially for anything other than "
           "pass. Then send it back with the text of NVDA's Speech Viewer.", "",
           "Date:", "Windows version:", "NVDA version (Insert+N, then H, then A shows it):", "", "## Setup", ""]
    out += [fill(s["setup"], f).strip()] if isinstance(s["setup"], str) else [f"- {fill(x, f)}" for x in s["setup"]]
    for part, title in checklist["parts"].items():
        if not any(c["part"] == part for c in checklist["check"]):
            continue
        out += ["", f"## {title}", ""]
        for c in checklist["check"]:
            if c["part"] == part:
                rows = f' (rows {", ".join(c["rows"])})' if c["rows"] else ""
                out += [f'### {c["id"]}. {c["title"]}{rows}', "", fill(c["steps"], f).strip(), "",
                        f'Expected: {c["expect"]}', "", "Result:", "Note:", ""]
    return "\n".join(out) + "\n"


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    build(Path(args[0]), "--final" in sys.argv)


if __name__ == "__main__":
    main()
