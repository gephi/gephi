#!/usr/bin/env python3
"""Unpacks windows-measurements.zip from the Windows kit into <report dir>/measurements/windows and summarizes.

Usage: import_windows.py <windows-measurements.zip> <report dir>

Windows PowerShell's Compress-Archive stores paths with backslashes, which unzip on macOS keeps in file names, so the
archive is extracted here, converting separators and refusing entries outside the windows folder.
"""
import json
import subprocess
import sys
import zipfile
from pathlib import Path, PurePosixPath


def main():
    archive, report_dir = sys.argv[1], Path(sys.argv[2])
    dest = (report_dir / "measurements").resolve()
    count = 0
    with zipfile.ZipFile(archive) as z:
        for info in z.infolist():
            rel = PurePosixPath(info.filename.replace("\\", "/"))
            if rel.is_absolute() or ".." in rel.parts or not rel.parts or rel.parts[0] != "windows":
                sys.exit(f"Unexpected entry {info.filename}")
            target = (dest / rel).resolve()
            if dest not in target.parents:
                sys.exit(f"Entry outside the measurements folder: {info.filename}")
            if info.filename.endswith(("/", "\\")):
                target.mkdir(parents=True, exist_ok=True)
                continue
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(z.read(info))
            count += 1
    print(f"{count} files extracted to {dest / 'windows'}")
    data = json.loads((dest / "windows" / "measurements.json").read_text(encoding="utf-8"))
    print(f"Windows run of {data.get('date')}, agent version {data.get('agentVersion')}")
    if not data.get("keyboardChecks"):
        print("WARNING: no keyboard checks in this run: the PC ran an old kit; rebuild it and run it again")
    subprocess.run([sys.executable, str(Path(__file__).with_name("summarize.py")), str(dest)], check=True)


if __name__ == "__main__":
    main()
