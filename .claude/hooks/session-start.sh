#!/usr/bin/env bash
# SessionStart hook — warm the Gradle cache (distribution + plugin classpath) so
# builds in Claude Code on the web don't stall on first-time downloads.
# Ported from Re-Claw; this repo has a single Gradle root.
#
# Also bootstraps the Android SDK (cmdline-tools + licenses + local.properties)
# into /opt/android-sdk — the web container ships no SDK; AGP auto-installs the
# platform/build-tools it needs during the first build once licenses are accepted.
#
# Design: remote-only, synchronous, idempotent, and NON-FATAL. A blocked host or
# missing tool must warn, not abort session start.
set -uo pipefail

# Claude Code on the web only — local checkouts already have warm caches.
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

ROOT="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"
log() { echo "[session-start] $*"; }

# Seed the wrapper's distribution cache for <version> from a matching local Gradle
# install, so a network download is never needed. No-op if already cached or if no
# matching local install exists.
seed_dist_from_local() {
  local dir="$1" ver="$2"
  [ -n "$ver" ] || return 0
  if ls -d "$HOME"/.gradle/wrapper/dists/gradle-"$ver"-bin/*/gradle-"$ver"/bin/gradle >/dev/null 2>&1; then
    return 0
  fi
  local src="" cand
  for cand in "/opt/gradle-$ver" "/opt/gradle"; do
    if [ -x "$cand/bin/gradle" ] && "$cand/bin/gradle" --version 2>/dev/null | grep -q "Gradle $ver"; then
      src="$cand"; break
    fi
  done
  [ -n "$src" ] || return 0
  local hd
  hd=$(ls -d "$HOME"/.gradle/wrapper/dists/gradle-"$ver"-bin/*/ 2>/dev/null | head -1)
  if [ -z "$hd" ]; then
    ( cd "$dir" && timeout 90 ./gradlew --version >/dev/null 2>&1 ) || true
    hd=$(ls -d "$HOME"/.gradle/wrapper/dists/gradle-"$ver"-bin/*/ 2>/dev/null | head -1)
  fi
  [ -n "$hd" ] || return 0
  rm -f "$hd"/*.lck "$hd"/*.part
  [ -d "$hd/gradle-$ver" ] || cp -a "$src" "$hd/gradle-$ver"
  touch "$hd/gradle-$ver-bin.zip.ok"
  log "seeded Gradle $ver distribution from $src"
}

# Android SDK: install cmdline-tools and accept licenses if missing.
SDK_ROOT=/opt/android-sdk
if [ ! -d "$SDK_ROOT/cmdline-tools/latest" ]; then
  log "installing Android SDK cmdline-tools ..."
  TOOLS_ZIP=$(mktemp -u /tmp/cmdtools-XXXX.zip)
  if curl -fsSLo "$TOOLS_ZIP" https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
     && mkdir -p "$SDK_ROOT/cmdline-tools" \
     && unzip -q "$TOOLS_ZIP" -d "$SDK_ROOT/cmdline-tools" \
     && mv "$SDK_ROOT/cmdline-tools/cmdline-tools" "$SDK_ROOT/cmdline-tools/latest"; then
    log "cmdline-tools installed"
  else
    log "WARN: could not install Android SDK cmdline-tools (dl.google.com blocked?) — Android builds will fail until it is available."
  fi
  rm -f "$TOOLS_ZIP"
fi
if [ -x "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ] && [ ! -f "$SDK_ROOT/licenses/android-sdk-license" ]; then
  yes | "$SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" --sdk_root="$SDK_ROOT" --licenses >/dev/null 2>&1 || true
fi
[ -f "$ROOT/local.properties" ] || echo "sdk.dir=$SDK_ROOT" > "$ROOT/local.properties"

if [ ! -x "$ROOT/gradlew" ]; then
  log "no gradlew, nothing to warm"
  exit 0
fi

VER=$(sed -n 's#.*/gradle-\([0-9][0-9.]*\)-bin\.zip#\1#p' "$ROOT/gradle/wrapper/gradle-wrapper.properties" 2>/dev/null | head -1)
seed_dist_from_local "$ROOT" "$VER"
log "warming Gradle (distribution + plugin classpath) ..."
if ( cd "$ROOT" && ./gradlew --console=plain --quiet help ); then
  log "warm-up OK"
else
  log "WARN: warm-up did not complete — a required host (services.gradle.org, dl.google.com, repo1.maven.org) may be blocked. The first real build will fetch what is missing."
fi
log "done"
exit 0
