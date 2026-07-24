#!/usr/bin/env bash
# SessionStart hook — warm the Gradle cache (distribution + plugin classpath) so
# builds in Claude Code on the web don't stall on first-time downloads.
# Ported from Re-Claw; this repo has a single Gradle root.
#
# Design: remote-only, synchronous, idempotent, and NON-FATAL. A blocked host or
# incompatible JVM must warn, not abort session start.
#
# Known ceiling: this repo pins Gradle 7.0.2 / AGP 7.0.3, which require Java <= 16
# to run, while the web container ships only Java 21. Until the wrapper/AGP are
# upgraded (or the environment adds an older JDK), the warm-up detects this and
# skips with a WARN instead of wasting time on a doomed download.
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

if [ ! -x "$ROOT/gradlew" ]; then
  log "no gradlew, nothing to warm"
  exit 0
fi

VER=$(sed -n 's#.*/gradle-\([0-9][0-9.]*\)-bin\.zip#\1#p' "$ROOT/gradle/wrapper/gradle-wrapper.properties" 2>/dev/null | head -1)

# Gradle < 7.3 cannot run on Java 17+; bail early with a clear message instead of
# downloading a distribution that can't start.
JAVA_MAJOR=$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\).*/\1/p' | head -1)
case "$VER" in
  7.[012]*|[0-6].*)
    if [ "${JAVA_MAJOR:-0}" -gt 16 ]; then
      log "WARN: Gradle $VER needs Java <= 16 but only Java $JAVA_MAJOR is installed — skipping warm-up. Upgrade the Gradle wrapper/AGP to build in web sessions."
      exit 0
    fi
    ;;
esac

seed_dist_from_local "$ROOT" "$VER"
log "warming Gradle (distribution + plugin classpath) ..."
if ( cd "$ROOT" && ./gradlew --console=plain --quiet help ); then
  log "warm-up OK"
else
  log "WARN: warm-up did not complete — a required host (services.gradle.org, dl.google.com, repo1.maven.org) may be blocked. The first real build will fetch what is missing."
fi
log "done"
exit 0
