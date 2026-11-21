#!/usr/bin/env python3
"""Reads a filled-in manual checklist (checklist.md from generate.py) into manual.json for generate.py.

Usage: import_manual.py <filled checklist.md> <report dir>

Unanswered checks are left out, so generate.py keeps the rows that need them provisional. Prints what it read so
the notes can be reviewed before updating rows.toml.
"""
import json
import re
import sys
from pathlib import Path

RESULTS = {"pass", "partial", "fail", "skip"}


def main():
    text = Path(sys.argv[1]).read_text(encoding="utf-8-sig").replace("\r\n", "\n")
    report_dir = Path(sys.argv[2])
    manual = {}
    session = {}
    for key, field in (("date", "Date"), ("os", "Windows version"), ("nvda", "NVDA version")):
        m = re.search(rf"^{field}[^:\n]*:[ \t]*(.*)$", text, re.MULTILINE)
        if m and m.group(1).strip():
            session[key] = m.group(1).strip()
    problems = []
    for sec in re.split(r"^### ", text, flags=re.MULTILINE)[1:]:
        check_id = sec.split(".", 1)[0].strip()
        result = re.search(r"^Result:[ \t]*(.*)$", sec, re.MULTILINE)
        note = re.search(r"^Note:[ \t]*(.*?)(?=^## |\Z)", sec, re.MULTILINE | re.DOTALL)
        value = result.group(1).strip().lower() if result else ""
        if not value:
            continue
        word = value.split()[0].strip(".,;:")
        if word not in RESULTS:
            problems.append(f"{check_id}: result '{value}' is not one of {sorted(RESULTS)}")
            continue
        manual[check_id] = {"result": word, "note": note.group(1).strip() if note else ""}
    if session:
        manual["_session"] = session
    out = report_dir / "manual.json"
    out.write_text(json.dumps(manual, indent=1, ensure_ascii=False), encoding="utf-8")
    for k, v in manual.items():
        if k != "_session":
            print(f'{k}: {v["result"]}' + (f' ({v["note"][:100]})' if v["note"] else ""))
    print(f"session: {session}")
    for p in problems:
        print("PROBLEM " + p)
    print(f"Wrote {out}")


if __name__ == "__main__":
    main()
