#!/usr/bin/env bash
# Runs the accessibility audit agent inside an official Gephi release and collects its measurements.
#
# Usage: measure.sh <version> [--variants default,fontsize] [--dist <Gephi.app | install dir>] [--out <dir>]
#
#   <version>   Gephi release, e.g. 0.11.4. Without --dist, the macOS or Linux build is downloaded from the GitHub
#               release (needs gh). On Windows, install Gephi and pass its install directory with --dist.
#   --variants  default: the out-of-the-box UI; fontsize: launched with --fontsize 28 (200% of the 14 pt default)
#   --out       defaults to reports/accessibility/<version>/measurements in the current directory
#
# Gephi opens on screen for about 3 minutes per variant and must stay the active application: leave the computer
# idle meanwhile. If another application takes the focus, the keyboard walks are marked interrupted, the agent
# stops after three of them, and generate.py refuses walk results until measure.sh is rerun.
set -euo pipefail

SKILL_DIR="$(cd "$(dirname "$0")/.." && pwd)"
# The Les Miserables sample of the Welcome screen, from this checkout
SAMPLE="$SKILL_DIR/../../../modules/WelcomeScreen/src/main/resources/org/gephi/desktop/welcome/samples/Les Miserables.gexf"
VERSION="${1:?usage: measure.sh <version> [--variants default,fontsize] [--dist dir] [--out dir]}"
shift
VARIANTS="default,fontsize"
DIST=""
OUT="reports/accessibility/$VERSION/measurements"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --variants) VARIANTS="$2"; shift 2 ;;
    --dist) DIST="$2"; shift 2 ;;
    --out) OUT="$2"; shift 2 ;;
    *) echo "unknown option $1" >&2; exit 1 ;;
  esac
done
CACHE="${XDG_CACHE_HOME:-$HOME/.cache}/gephi-a11y-audit"
mkdir -p "$OUT" "$CACHE"
OUT="$(cd "$OUT" && pwd)"

case "$(uname -s)" in
  Darwin) OS=macos ;;
  Linux) OS=linux ;;
  MINGW*|MSYS*|CYGWIN*) OS=windows ;;
  *) echo "unsupported OS $(uname -s)" >&2; exit 1 ;;
esac
case "$(uname -m)" in
  arm64|aarch64) ARCH=aarch64 ;;
  *) ARCH=x64 ;;
esac

# Release download, kept in a cache outside the repository
if [[ -z "$DIST" ]]; then
  DL="$CACHE/$VERSION-$OS-$ARCH"
  mkdir -p "$DL"
  if [[ "$OS" == macos ]]; then
    DIST="$DL/Gephi.app"
    if [[ ! -d "$DIST" ]]; then
      gh release download "v$VERSION" -R gephi/gephi -p "gephi-$VERSION-macos-$ARCH.dmg" -D "$DL" --clobber
      MNT="$DL/mnt"
      mkdir -p "$MNT"
      hdiutil attach -nobrowse -readonly -mountpoint "$MNT" "$DL/gephi-$VERSION-macos-$ARCH.dmg" >/dev/null
      cp -R "$MNT/Gephi.app" "$DIST"
      hdiutil detach "$MNT" >/dev/null
      rm -f "$DL/gephi-$VERSION-macos-$ARCH.dmg"
    fi
  elif [[ "$OS" == linux ]]; then
    if [[ ! -d "$DL/gephi-$VERSION" ]]; then
      gh release download "v$VERSION" -R gephi/gephi -p "gephi-$VERSION-linux-$ARCH.tar.gz" -D "$DL" --clobber
      tar -xzf "$DL/gephi-$VERSION-linux-$ARCH.tar.gz" -C "$DL"
      rm -f "$DL/gephi-$VERSION-linux-$ARCH.tar.gz"
    fi
    DIST="$DL/gephi-$VERSION"
  else
    echo "On Windows, install Gephi $VERSION and pass its directory with --dist" >&2
    exit 1
  fi
fi

if [[ -x "$DIST/Contents/MacOS/gephi" ]]; then
  LAUNCHER="$DIST/Contents/MacOS/gephi"
elif [[ -f "$DIST/bin/gephi64.exe" ]]; then
  LAUNCHER="$DIST/bin/gephi64.exe"
elif [[ -x "$DIST/bin/gephi" ]]; then
  LAUNCHER="$DIST/bin/gephi"
else
  echo "No Gephi launcher in $DIST" >&2
  exit 1
fi

# A11Y_AGENT_JAR skips the build, e.g. on a Windows PC without a JDK (copy the jar built on another machine)
AGENT="${A11Y_AGENT_JAR:-$("$SKILL_DIR/scripts/build-agent.sh" "$CACHE/a11y-audit-agent.jar")}"
PYTHON="$(command -v python3 || command -v python)"

# Paths handed to Java must be native on Windows (Git Bash gives /c/...)
native() {
  if command -v cygpath >/dev/null 2>&1; then cygpath -w "$1"; else echo "$1"; fi
}

run_variant() {
  local variant="$1"
  # macOS is the reference platform; other platforms get their own folder, e.g. windows, windows-fontsize
  local name="$variant"
  if [[ "$OS" != macos ]]; then
    name="$OS"
    [[ "$variant" != default ]] && name="$OS-$variant"
  fi
  local dir="$OUT/$name"
  local run="$CACHE/run-$VERSION-$variant"
  rm -rf "$dir" "$run"
  mkdir -p "$dir" "$run/userdir/config/Preferences/org/gephi/desktop" \
    "$run/userdir/config/Preferences/org/gephi/welcome"
  # First-run prompts off: analytics consent, update check, welcome screen
  printf 'disable_all_tracking=true\nanalytics_consent_shown=true\ncheck_latest_version=false\n' \
    > "$run/userdir/config/Preferences/org/gephi/desktop/branding.properties"
  printf 'WelcomeScreen_Open_Startup=false\n' > "$run/userdir/config/Preferences/org/gephi/welcome/screen.properties"
  local args=(--userdir "$(native "$run/userdir")" --cachedir "$(native "$run/cachedir")")
  if [[ "$variant" == fontsize ]]; then
    args+=(--fontsize 28)
  fi
  local walks=true
  [[ "$variant" == fontsize ]] && walks=false
  args+=(
    "-J-javaagent:$(native "$AGENT")"
    "-J-Da11yaudit.out=$(native "$dir")"
    "-J-Da11yaudit.variant=$name"
    "-J-Da11yaudit.walks=$walks"
    "-J-Da11yaudit.graph=$(native "$SAMPLE")"
    "-J-Da11yaudit.broken=$(native "$SKILL_DIR/data/import-issues.gexf")"
    "-J-Duser.language=en" "-J-Duser.country=US"
    "-J-Dorg.gephi.crashReporter.enabled=false"
    "-J-Dorg.gephi.settingsUpgrader.enabled=false"
    "-J-Dplugin.manager.check.interval=NEVER"
    "-J-Dnetbeans.exception.report.min.level=99999"
    "-J-Dnetbeans.exception.alert.min.level=99999"
  )
  echo "Running $variant (Gephi will open on screen; don't touch the keyboard or mouse)..."
  local start=$SECONDS
  # macOS has no timeout(1); the agent's own watchdog writes partial results first
  if [[ "$OS" == macos ]]; then
    # Through LaunchServices, so Gephi is activated and gets keyboard focus like when double-clicked
    perl -e 'alarm 1200; exec @ARGV' open -W -n --stdout "$dir/console.log" --stderr "$dir/console.log" \
      "$DIST" --args "${args[@]}" || true
  else
    perl -e 'alarm 1200; exec @ARGV' "$LAUNCHER" "${args[@]}" > "$dir/console.log" 2>&1 || true
  fi
  echo "  $variant done in $((SECONDS - start)) s"
  if [[ ! -f "$dir/measurements.json" ]]; then
    echo "  no measurements.json, see $dir/console.log" >&2
    return 1
  fi
}

IFS=',' read -ra LIST <<< "$VARIANTS"
for v in "${LIST[@]}"; do
  run_variant "$v"
done
"$PYTHON" "$SKILL_DIR/scripts/summarize.py" "$OUT"
