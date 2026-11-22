#!/usr/bin/env python3
"""Turns the agent's raw measurements into per-criterion metrics.

Usage: summarize.py <measurements dir>

Reads <dir>/<variant>/measurements.json for each variant run and writes <dir>/summary.json (metrics the report rows
refer to) and <dir>/summary.md (the findings, for review). Standard library only.
"""
import json
import math
import re
import sys
from pathlib import Path

BUTTON_ROLES = {"push button", "toggle button", "check box", "radio button", "combo box", "slider", "spin box",
                "hyperlink", "page tab"}
INPUT_ROLES = {"text", "password text", "combo box", "spin box", "slider"}
TARGET_ROLES = BUTTON_ROLES
MIN_TARGET = 24


def norm(s):
    return re.sub(r"\s+", " ", re.sub(r"[^\w\s]", " ", (s or "").lower())).strip()


def label(n):
    """Short human description of a node."""
    bits = [n.get("role") or "?"]
    if n.get("name"):
        bits.append(f'"{n["name"]}"')
    elif n.get("text"):
        bits.append(f'text "{n["text"]}"')
    elif n.get("tooltip"):
        bits.append(f'tooltip "{n["tooltip"]}"')
    bits.append(n.get("class", "").split(".")[-1])
    if n.get("panel"):
        bits.append(f'in {n["panel"]}')
    return " ".join(bits)


def item(n, **extra):
    """A node as the report needs it: what it is, where, and a short label for humans."""
    d = {"label": label(n), "role": n.get("role"), "name": n.get("name"), "text": n.get("text"),
         "tooltip": n.get("tooltip"), "class": n.get("class", "").split(".")[-1], "panel": n.get("panel")}
    d.update(extra)
    return {k: v for k, v in d.items() if v is not None}


def is_large(n):
    size = n.get("fontSize") or 0
    return size >= 24 or (n.get("bold") and size >= 18.66)


def interactive(nodes):
    return [n for n in nodes if n.get("interactive") and not n.get("part")]


def target_failures(nodes):
    """WCAG 2.5.8: targets under 24x24 that also fail the spacing exception."""
    targets = [n for n in interactive(nodes) if n.get("role") in TARGET_ROLES and n.get("bounds")
               and n.get("enabled", True)]
    out = []
    for a in targets:
        x, y, w, h = a["bounds"]
        if w >= MIN_TARGET and h >= MIN_TARGET:
            continue
        cx, cy = x + w / 2, y + h / 2
        conflict = None
        for b in targets:
            if b is a:
                continue
            bx, by, bw, bh = b["bounds"]
            dx = max(bx - cx, 0, cx - (bx + bw))
            dy = max(by - cy, 0, cy - (by + bh))
            if math.hypot(dx, dy) < MIN_TARGET / 2:
                conflict = b
                break
            if (bw < MIN_TARGET or bh < MIN_TARGET) and \
                    math.hypot(cx - (bx + bw / 2), cy - (by + bh / 2)) < MIN_TARGET:
                conflict = b
                break
        if conflict:
            out.append(item(a, size=[w, h], near=label(conflict), nearSize=conflict["bounds"][2:]))
    return out


def scene_metrics(scene, label_font):
    nodes = scene["nodes"]
    inter = interactive(nodes)
    enabled = [n for n in inter if n.get("enabled", True)]
    unnamed = [n for n in inter if not (n.get("name") or "").strip()]
    not_focusable = [n for n in enabled if n.get("focusable") is False and n.get("role") in BUTTON_ROLES]
    label_mismatch = [n for n in inter if n.get("text") and n.get("name")
                      and norm(n["text"]) not in norm(n["name"])]
    contrast_fail = []
    for n in nodes:
        if n.get("contrast") is None or not n.get("enabled", True):
            continue
        if not n.get("text") and n.get("role") not in ("text", "table", "tree", "list"):
            continue
        required = 3.0 if is_large(n) else 4.5
        if n["contrast"] < required:
            contrast_fail.append(item(n, fg=n.get("fg"), bg=n.get("bg"), contrast=n["contrast"], required=required))
    clipped = [item(n) for n in nodes if n.get("clipped")]
    clipped_cells = []
    for n in nodes:
        for c in n.get("clippedCells") or []:
            clipped_cells.append(f'{c} ({n.get("name") or n.get("class", "").split(".")[-1]})')
    inputs_unlabelled = [item(n) for n in inter if n.get("role") in INPUT_ROLES
                         and not (n.get("name") or "").strip() and not n.get("labeledBy")]
    window_keys = sorted({k for n in nodes for k in n.get("windowKeys") or []})
    font_sizes = [n["fontSize"] for n in nodes if n.get("fontSize") and (n.get("text") or n.get("role") == "text")]
    canvas = [n for n in nodes if n.get("role") == "canvas"]
    small = [item(n, fontSize=n["fontSize"]) for n in nodes
             if n.get("text") and n.get("fontSize") and label_font and n["fontSize"] < 0.75 * label_font]
    links = sorted({n.get("name") or n.get("text") or "" for n in inter
                    if n.get("role") == "hyperlink" or "Hyperlink" in n.get("class", "")})
    m = {
        "kind": scene["kind"],
        "windows": [w.get("title") for w in scene["windows"]],
        "interactive": len(inter),
        "unnamed": [item(n) for n in unnamed],
        "unnamedWindowSystem": [item(n) for n in unnamed if n.get("windowSystem")],
        "buttonsEnabled": len([n for n in enabled if n.get("role") in BUTTON_ROLES]),
        "buttonsNotFocusable": [item(n) for n in not_focusable],
        "labelNotInName": [item(n) for n in label_mismatch],
        "pageTabs": len([n for n in nodes if n.get("role") == "page tab"]),
        "textContrastFailures": contrast_fail,
        "clippedText": clipped,
        "clippedCells": clipped_cells,
        "inputsWithoutName": inputs_unlabelled,
        "targetSizeFailures": target_failures(nodes),
        "windowScopedCharacterKeys": window_keys,
        "minFontSize": min(font_sizes) if font_sizes else None,
        "iconContrast": [item(n, contrast=n["iconContrast"]) for n in nodes if n.get("iconContrast") is not None],
        "smallText": small,
        "links": links,
        "canvas": [{"name": n.get("name"), "focusable": n.get("focusable")} for n in canvas],
        "tables": [{"name": n.get("name"), "rows": n.get("rows"), "headers": n.get("headers"),
                    "firstCell": n.get("firstCell"), "sampleCells": n.get("sampleCells")}
                   for n in nodes if n.get("role") == "table"],
        "panels": scene.get("panels"),
        "closedWith": scene.get("closedWith"),
    }
    m["walks"] = [walk_metrics(w, nodes) for w in scene.get("walks") or []]
    return m


def walk_metrics(w, nodes):
    steps = w.get("steps") or []

    def sized(s):
        b = nodes[s["node"]].get("bounds") if s.get("node") is not None and s["node"] < len(nodes) else None
        return not b or (b[2] > 0 and b[3] > 0)

    hidden = [s for s in steps if s.get("visibleFraction") == 0 and sized(s)]
    partly = [s for s in steps if 0 < (s.get("visibleFraction") if s.get("visibleFraction") is not None else 1) < 1]
    measured = [s for s in steps if s.get("focusIndicator") and s.get("role") != "canvas"
                and s.get("visibleFraction", 1) >= 0.99]
    # A dotted focus rectangle changes about half the outline; under 15% nothing recognisable changes
    invisible = [s for s in measured
                 if s["focusIndicator"]["changedPixels"] < 0.15 * s["focusIndicator"]["perimeter"]]
    low = [s for s in measured if s not in invisible and (s["focusIndicator"].get("medianContrast") or 0) < 3]
    stuck = w.get("stuckAt") or {}
    return {
        "scope": w["scope"],
        "outcome": w["outcome"],
        "interrupted": w.get("interrupted", False),
        "steps": len(steps),
        "order": [{"role": s.get("role"), "name": s.get("name"), "class": s["class"].split(".")[-1]} for s in steps],
        "unnamedStops": [{"role": s.get("role"), "name": s.get("name"), "class": s["class"].split(".")[-1],
                          "panel": s.get("panel")} for s in steps
                         if not (s.get("name") or "").strip() and s.get("role") != "canvas"],
        "stuckAt": f'{stuck.get("role")} "{stuck.get("name") or ""}" {stuck.get("class", "").split(".")[-1]}'
        if stuck else None,
        "ctrlTabExits": (stuck.get("ctrlTab") or {}).get("moved") if stuck else None,
        "leftTo": (w.get("leftTo") or {}).get("panel"),
        "windowsOpenedByTab": w.get("windowsOpenedByTab") or [],
        "includesCanvas": any(s.get("role") == "canvas" for s in steps),
        "shiftTabMoves": (w.get("shiftTab") or {}).get("moved"),
        "focusIndicatorMeasured": len(measured),
        "focusNotVisible": [{"role": s.get("role"), "name": s.get("name"), "class": s["class"].split(".")[-1],
                             "panel": s.get("panel"), "evidence": s["focusIndicator"].get("evidence")}
                            for s in invisible],
        "focusLowContrast": len(low),
        "focusHidden": [{"role": s.get("role"), "name": s.get("name"), "class": s["class"].split(".")[-1],
                         "panel": s.get("panel")} for s in hidden],
        "focusPartlyHidden": len(partly),
        "focusMedianContrasts": sorted({s["focusIndicator"].get("medianContrast") for s in measured
                                        if s["focusIndicator"].get("medianContrast")}),
    }


def menu_metrics(menus):
    items = []

    def walk(m, depth):
        for i in m.get("items") or []:
            items.append(i)
            walk(i, depth + 1)

    for m in menus:
        walk(m, 0)
    leaf = [i for i in items if "items" not in i]
    return {
        "topLevel": [m["text"] for m in menus],
        "topLevelWithoutMnemonic": [m["text"] for m in menus if not m.get("mnemonic")],
        "items": len(leaf),
        "itemsWithShortcut": len([i for i in leaf if i.get("accelerator")]),
        "itemsWithMnemonic": len([i for i in items if i.get("mnemonic")]),
    }


def events_metrics(run):
    if not run:
        return None
    by_prop = {}
    for e in run.get("events") or []:
        by_prop[e["property"]] = by_prop.get(e["property"], 0) + 1
    status = [e for e in run.get("events") or []
              if e.get("role") in ("status bar", "progress bar") or "status" in (e.get("name") or "").lower()]
    return {"events": len(run.get("events") or []), "byProperty": by_prop, "statusOrProgressEvents": len(status),
            "focusMoved": run.get("focusMoved"), "reportOpened": run.get("reportOpened")}


def variant_metrics(data):
    theme = data.get("theme") or {}
    pairs = theme.get("pairs") or []
    return {
        "variant": data.get("variant"),
        "status": data.get("status"),
        "date": data.get("date"),
        "foreground": data.get("foreground"),
        "failedSteps": [l.split("\n")[0] for l in data.get("log") or [] if "FAILED" in l],
        "runtime": data.get("runtime"),
        "windowSize": data.get("windowSize"),
        "theme": {"lookAndFeel": theme.get("lookAndFeel"), "labelFontSize": theme.get("labelFontSize"),
                  "installed": theme.get("installedLookAndFeels"), "focusWidth": theme.get("focusWidth"),
                  "pairs": pairs,
                  "failingPairs": [p for p in pairs if p["contrast"] < p["required"]]},
        "tooltips": data.get("tooltips"),
        "menus": menu_metrics(data.get("menus") or []),
        "keymap": {"shortcuts": len((data.get("keymap") or {}).get("bindings") or []),
                   "characterKeys": (data.get("keymap") or {}).get("characterKeys")},
        "layoutRun": events_metrics(data.get("layoutRun")),
        "statisticRun": events_metrics(data.get("statisticRun")),
        "keyboardChecks": data.get("keyboardChecks"),
        "scenes": {s["name"]: scene_metrics(s, theme.get("labelFontSize")) for s in data.get("scenes") or []},
        "gephiLogWarnings": len(data.get("gephiLog") or []),
    }


def markdown(summary):
    out = []
    for name, v in summary["variants"].items():
        rt = v.get("runtime") or {}
        out.append(f'# Variant {name}\n')
        out.append(f'{rt.get("title")} on {rt.get("os.name")} {rt.get("os.version")} {rt.get("os.arch")}, '
                   f'Java {rt.get("java.runtime.version")}, {v["theme"]["lookAndFeel"]} '
                   f'{v["theme"]["labelFontSize"]} pt, window {v.get("windowSize")}, measured {v.get("date")}\n')
        if v["failedSteps"]:
            out.append("Failed steps:\n" + "\n".join(f"- {s.strip()}" for s in v["failedSteps"]) + "\n")
        if not v.get("foreground"):
            out.append("**Gephi never had keyboard focus: keyboard walks are invalid.**\n")
        out.append(f'Tooltips: {v["tooltips"]}\n')
        out.append("Theme pairs below the WCAG minimum:\n" + "\n".join(
            f'- {p["foreground"]} {p["fg"]} on {p["background"]} {p["bg"]}: {p["contrast"]}:1 '
            f'(needs {p["required"]})' for p in v["theme"]["failingPairs"]) + "\n")
        mm = v["menus"]
        out.append(f'Menus: {mm["topLevel"]}, without mnemonic {mm["topLevelWithoutMnemonic"]}, '
                   f'{mm["itemsWithShortcut"]} of {mm["items"]} items with a shortcut, '
                   f'{mm["itemsWithMnemonic"]} with a mnemonic. Global shortcuts: {v["keymap"]["shortcuts"]}, '
                   f'character keys: {v["keymap"]["characterKeys"]}\n')
        out.append(f'Layout run events: {v["layoutRun"]}\n\nStatistic run events: {v["statisticRun"]}\n')
        for sname, s in v["scenes"].items():
            out.append(f'\n## {sname} ({s["kind"]}: {s["windows"]})\n')
            out.append(f'- interactive controls: {s["interactive"]}, enabled buttons: {s["buttonsEnabled"]}')
            for key in ("unnamed", "buttonsNotFocusable", "inputsWithoutName", "labelNotInName", "clippedText",
                        "clippedCells", "smallText", "links", "windowScopedCharacterKeys"):
                if s[key]:
                    out.append(f'- {key} ({len(s[key])}): ' + "; ".join(
                        x if isinstance(x, str) else x["label"] + (f' shows "{x["text"]}"' if key == "labelNotInName"
                                                                   else f' {x["fontSize"]:g} pt' if key == "smallText"
                                                                   else "") for x in s[key]))
            if s["textContrastFailures"]:
                out.append(f'- textContrastFailures ({len(s["textContrastFailures"])}): ' + "; ".join(
                    f'{f["label"]} {f["contrast"]}:1' for f in s["textContrastFailures"]))
            if s["targetSizeFailures"]:
                out.append(f'- targetSizeFailures ({len(s["targetSizeFailures"])}): ' + "; ".join(
                    f'{f["label"]} {f["size"]} near {f["near"]}' for f in s["targetSizeFailures"]))
            if s["canvas"]:
                out.append(f'- canvas: {s["canvas"]}')
            for w in s["walks"]:
                out.append(f'- walk {w["scope"]}: {w["outcome"]} after {w["steps"]} stops'
                           + (f', stuck at {w["stuckAt"]}, Ctrl+Tab exits: {w["ctrlTabExits"]}' if w["stuckAt"] else "")
                           + (f', focus not visible on {[(x.get("role") or "?") + " " + (x.get("name") or "") for x in w["focusNotVisible"]]}'
                              if w["focusNotVisible"] else "")
                           + f', focus change contrast {w["focusMedianContrasts"]}')
    return "\n".join(out) + "\n"


def main():
    root = Path(sys.argv[1])
    variants = {}
    for f in sorted(root.glob("*/measurements.json")):
        variants[f.parent.name] = variant_metrics(json.loads(f.read_text(encoding="utf-8")))
    summary = {"variants": variants}
    (root / "summary.json").write_text(json.dumps(summary, indent=1, ensure_ascii=False), encoding="utf-8")
    (root / "summary.md").write_text(markdown(summary), encoding="utf-8")
    print(f"Wrote {root / 'summary.json'} and {root / 'summary.md'}")


if __name__ == "__main__":
    main()
