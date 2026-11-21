#!/usr/bin/env bash
# Packs what the Windows PC needs (agent jar, run script, sample files, checklist) into
# reports/accessibility/<version>/gephi-a11y-windows-kit.zip. Run generate.py first, for manual-checklist.md.
# Usage: make-windows-kit.sh <version>
set -euo pipefail
SKILL_DIR="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="${1:?usage: make-windows-kit.sh <version>}"
REPORT="reports/accessibility/$VERSION"
[[ -f "$REPORT/manual-checklist.md" ]] || { echo "No $REPORT/manual-checklist.md: run generate.py first" >&2; exit 1; }
# The folder name carries the build time, so an old kit on the PC can't be mistaken for this one
BUILT="$(date +%Y%m%d-%H%M)"
STAGE="$(mktemp -d)/gephi-a11y-$VERSION-$BUILT"
mkdir -p "$STAGE"
"$SKILL_DIR/scripts/build-agent.sh" "$STAGE/a11y-audit-agent.jar" >/dev/null
AGENT_VERSION="$(sed -n 's/.*static final int VERSION = \([0-9]*\);.*/\1/p' \
  "$SKILL_DIR/agent/src/org/gephi/a11yaudit/AuditAgent.java")"
printf 'Gephi %s accessibility kit, agent version %s, built %s\r\n' "$VERSION" "$AGENT_VERSION" "$BUILT" \
  > "$STAGE/kit-version.txt"
cp "$SKILL_DIR/data/import-issues.gexf" "$STAGE/"
cp "$SKILL_DIR/../../../modules/WelcomeScreen/src/main/resources/org/gephi/desktop/welcome/samples/Les Miserables.gexf" "$STAGE/les-miserables.gexf"
cp "$REPORT/manual-checklist.md" "$STAGE/checklist.md"
sed "s/{version}/$VERSION/g" "$SKILL_DIR/scripts/windows/measure-windows.ps1" > "$STAGE/measure-windows.ps1"
sed "s/{version}/$VERSION/g" "$SKILL_DIR/scripts/windows/README.txt" | sed 's/$/\r/' > "$STAGE/README.txt"
ZIP="$(cd "$REPORT" && pwd)/gephi-a11y-windows-kit.zip"
rm -f "$ZIP"
(cd "$(dirname "$STAGE")" && zip -qr "$ZIP" "$(basename "$STAGE")")
echo "$ZIP"
