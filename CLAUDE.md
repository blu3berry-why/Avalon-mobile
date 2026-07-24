# Avalon (Android)

Android app (Kotlin, Hilt, Retrofit) — Gradle 7.0.2, AGP 7.0.3, compileSdk 31.

## Environment

The build requires **JDK 11** (Gradle 7.0.2 does not run on Java 17+) and the
Android SDK (platform 31, build-tools 30.0.3). In Claude Code on the web these
are installed by `.claude/hooks/session-start.sh`, which also sets `JAVA_HOME`
and `ANDROID_HOME` for the session.

## Commands

- Build: `./gradlew :app:assembleDebug`
- Unit tests: `./gradlew :app:testDebugUnitTest`
- Lint: `./gradlew :app:lintDebug`

Note: `lintDebug` currently fails on a pre-existing `MissingDefaultResource`
error (`app/src/main/res/layout-port/auth_login.xml` has no base `layout/`
counterpart).
