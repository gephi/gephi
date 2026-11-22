#!/usr/bin/env python3
"""Lists English UI strings that may rely on sensory characteristics (WCAG 1.3.3) or a pointer (2.5.x).

Usage: scan_strings.py <gephi repo> <tag> <output.json>

Reads every English Bundle.properties at the release tag through git, so the working tree can be on any branch,
and keeps the strings that mention position, shape, colour, sound or mouse gestures. The list is evidence for a
reviewer: most hits are fine (e.g. "Rotate 45° left" names an action, not where a control is).
"""
import json
import re
import subprocess
import sys

PATTERNS = {
    "position": r"\b(on the (left|right)|at the (top|bottom)|above|below|upper|lower|top-right|top-left|"
                r"bottom-right|bottom-left|next to|beside)\b",
    "shape": r"\b(round|square|circle|triangle|arrow icon|icon)\b",
    "colour": r"\b(red|green|blue|yellow|orange|grey|gray)\b",
    "sound": r"\b(sound|beep|hear)\b",
    "pointer": r"\b(click|double-click|right[- ]click|left[- ]click|drag|drop|hover|mouse|wheel)\b",
}


def git(repo, *args):
    return subprocess.run(["git", "-C", repo, *args], check=True, capture_output=True, text=True).stdout


def parse(text):
    """Properties entries, joining continuation lines and decoding \\uXXXX escapes."""
    entries = {}
    lines = text.splitlines()
    i = 0
    while i < len(lines):
        line = lines[i].strip()
        i += 1
        if not line or line.startswith(("#", "!")):
            continue
        while line.endswith("\\") and i < len(lines):
            line = line[:-1] + lines[i].strip()
            i += 1
        m = re.match(r"([^=:\s]+)\s*[=:\s]\s*(.*)", line)
        if m:
            value = re.sub(r"\\u([0-9a-fA-F]{4})", lambda u: chr(int(u.group(1), 16)), m.group(2))
            entries[m.group(1)] = value.replace("\\n", " ").replace("\\", "")
    return entries


def main():
    repo, tag, out = sys.argv[1:4]
    files = [f for f in git(repo, "ls-tree", "-r", "--name-only", tag).splitlines()
             if f.endswith("/Bundle.properties") and "/src/main/" in f]
    hits = []
    for f in files:
        for key, value in parse(git(repo, "show", f"{tag}:{f}")).items():
            if key.startswith(("OpenIDE-Module", "CTL_", "ACSD_")) and "Module" in key:
                continue
            kinds = [k for k, p in PATTERNS.items() if re.search(p, value, re.IGNORECASE)]
            if kinds:
                hits.append({"file": f, "key": key, "text": value[:300], "kinds": kinds})
    with open(out, "w", encoding="utf-8") as fh:
        json.dump({"tag": tag, "files": len(files), "hits": hits}, fh, indent=1, ensure_ascii=False)
    by_kind = {}
    for h in hits:
        for k in h["kinds"]:
            by_kind[k] = by_kind.get(k, 0) + 1
    print(f"{len(hits)} strings in {len(files)} bundles: {by_kind}; wrote {out}")


if __name__ == "__main__":
    main()
