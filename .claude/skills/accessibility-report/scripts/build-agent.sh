#!/usr/bin/env bash
# Builds the audit agent jar with a JDK 17 or newer.
# Usage: build-agent.sh [output jar], default <skill>/agent/build/a11y-audit-agent.jar
set -euo pipefail

SKILL_DIR="$(cd "$(dirname "$0")/.." && pwd)"
SRC="$SKILL_DIR/agent/src"
BUILD="$SKILL_DIR/agent/build"
JAR="${1:-$BUILD/a11y-audit-agent.jar}"

java_major() {
  "$1/bin/javac" -version 2>&1 | sed -E 's/^javac ([0-9]+).*/\1/'
}

find_jdk() {
  local candidates=()
  [[ -n "${A11Y_JDK:-}" ]] && candidates+=("$A11Y_JDK")
  [[ -n "${JAVA_HOME:-}" ]] && candidates+=("$JAVA_HOME")
  for d in "$HOME"/.sdkman/candidates/java/*; do candidates+=("$d"); done
  if command -v javac >/dev/null 2>&1; then
    candidates+=("$(dirname "$(dirname "$(readlink -f "$(command -v javac)" 2>/dev/null || command -v javac)")")")
  fi
  for jdk in "${candidates[@]}"; do
    if [[ -x "$jdk/bin/javac" ]] && (( $(java_major "$jdk") >= 17 )); then
      echo "$jdk"
      return
    fi
  done
  echo "No JDK 17+ found; set A11Y_JDK" >&2
  exit 1
}

JDK="$(find_jdk)"
rm -rf "$BUILD/classes"
mkdir -p "$BUILD/classes" "$(dirname "$JAR")"
"$JDK/bin/javac" --release 17 -Xlint:-options -d "$BUILD/classes" $(find "$SRC" -name '*.java')
printf 'Premain-Class: org.gephi.a11yaudit.AuditAgent\n' > "$BUILD/manifest.txt"
"$JDK/bin/jar" cfm "$JAR" "$BUILD/manifest.txt" -C "$BUILD/classes" .
echo "$JAR"
