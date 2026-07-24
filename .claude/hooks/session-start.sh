#!/bin/bash
# SessionStart hook for Claude Code on the web.
# Installs the toolchain this project needs (JDK 11 + Android SDK 31) and
# pre-warms the Gradle cache so Android builds/tests/lint work in cloud sessions.
#
# Project constraints: Gradle 7.0.2 + AGP 7.0.3 require Java 11 (they do not
# run on the container's default Java 21). compileSdk is 31.
set -euo pipefail

# Only needed in the remote (cloud) environment; local machines have their own setup.
if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

JDK11_HOME=/usr/lib/jvm/java-11-openjdk-amd64
ANDROID_SDK=/opt/android-sdk
CMDLINE_TOOLS_URL=https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip

# --- JDK 11 (required by Gradle 7.0.2 / AGP 7.0.3) ---
if [ ! -x "$JDK11_HOME/bin/java" ]; then
  echo "Installing OpenJDK 11..."
  sudo apt-get update -qq || true
  sudo apt-get install -y -qq openjdk-11-jdk-headless
fi

# --- Android SDK: cmdline-tools, platform 31, build-tools 30.0.3 ---
if [ ! -x "$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager" ]; then
  echo "Installing Android command-line tools..."
  tmpzip=$(mktemp /tmp/cmdline-tools-XXXX.zip)
  curl -fsSLo "$tmpzip" "$CMDLINE_TOOLS_URL"
  sudo mkdir -p "$ANDROID_SDK/cmdline-tools"
  sudo unzip -q -o "$tmpzip" -d "$ANDROID_SDK/cmdline-tools"
  sudo mv -T "$ANDROID_SDK/cmdline-tools/cmdline-tools" "$ANDROID_SDK/cmdline-tools/latest"
  rm -f "$tmpzip"
fi

if [ ! -d "$ANDROID_SDK/platforms/android-31" ] || [ ! -d "$ANDROID_SDK/build-tools/30.0.3" ]; then
  echo "Installing Android SDK packages (platform 31, build-tools 30.0.3)..."
  yes | sudo -E "$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null || true
  sudo -E "$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager" --install \
    "platform-tools" "platforms;android-31" "build-tools;30.0.3" > /dev/null
fi

# --- Environment for the session ---
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  {
    echo "export JAVA_HOME=$JDK11_HOME"
    echo "export ANDROID_HOME=$ANDROID_SDK"
    echo "export ANDROID_SDK_ROOT=$ANDROID_SDK"
    echo 'export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"'
  } >> "$CLAUDE_ENV_FILE"
fi

# AGP also finds the SDK via local.properties (gitignored).
echo "sdk.dir=$ANDROID_SDK" > "$CLAUDE_PROJECT_DIR/local.properties"

# --- Pre-warm Gradle (wrapper distribution + dependency cache) ---
# Best-effort: a broken build on some branch must not block session startup.
cd "$CLAUDE_PROJECT_DIR"
chmod +x gradlew
export JAVA_HOME=$JDK11_HOME ANDROID_HOME=$ANDROID_SDK ANDROID_SDK_ROOT=$ANDROID_SDK
./gradlew --no-daemon :app:compileDebugKotlin -q < /dev/null \
  || echo "Warning: Gradle pre-warm failed; toolchain is installed but dependencies may download on first build."

echo "Android environment ready: JDK 11, Android SDK 31 at $ANDROID_SDK"
