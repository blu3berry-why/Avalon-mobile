# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Kotlin Multiplatform (Android / iOS / desktop-JVM) rewrite of the Avalon board-game companion app, using Compose Multiplatform for shared UI. The backend is the avalon-spring server (separate repo); the migration plan lives in the Avalon-Boardgame-with-Kubernetes repo under `doc/avalon-kmp-migration-plan.md`.

`app/` and `testing/` at the repo root are the **legacy** pre-KMP Android app and scripts — not included in `settings.gradle.kts`, do not touch or reference them.

## Mirrored infrastructure — do not diverge, do not re-flag

This project deliberately mirrors the Re-Claw reference stack (`~/StudioProjects/Re-Claw/app`). The mirrored surfaces are:

- `core/domain/.../result/` — `Result`, `DataError`, `ResultExt` (two-param `Success<out D, out E>` shape, byte-identical to Re-Claw's)
- `build-logic/convention/` plugins, including `PathUtil`'s underscore namespaces (`:core:data` → `hu.blu3berry.avalon_core_data`)
- MVI presentation scaffolding (State/Action/Event, as it lands in later phases)

Reviewer suggestions that are *style* improvements to these shapes (e.g. `Nothing`-typed `Result` variants, dotted package names) are pre-cleared skips — cite this section instead of applying them.

**Upstream first:** genuine improvements to mirrored infra land in Re-Claw first, then propagate here. Bugs are exempt — fix immediately in both repos.

## Commands

```bash
# Full check for the data layer (unit tests run on the desktop target)
./gradlew :core:data:desktopTest :core:domain:desktopTest

# Single test class / method
./gradlew :core:data:desktopTest --tests "hu.blu3berry.avalon.core.data.repository.GameRepositoryImplTest"

# Android app (release variant matters: it exercises the deny-all network security config)
./gradlew :androidApp:assembleDebug :androidApp:assembleRelease

# Desktop app
./gradlew :composeApp:run

# Regenerate API clients after editing specs in core/data/openapi/
./gradlew :core:data:kmpgenGenerateAll
```

In Claude Code on the web, `.claude/hooks/session-start.sh` bootstraps the Android
SDK into `/opt/android-sdk`, writes `local.properties`, and warms the Gradle
distribution named in `gradle/wrapper/gradle-wrapper.properties`.

Gradle **cannot run inside the agent sandbox** (socket bind denial, not filesystem — `./gradlew --version` succeeding proves nothing). See `docs/dev-env-gotchas.md` before debugging build failures, and for the list of pre-existing warnings that are noise.

## Architecture

Module graph (typesafe project accessors, e.g. `projects.core.domain`):

- `:core:domain` — pure Kotlin: models, `GameRepository` interface, `Result`/`DataError`, `SessionManager`/`AuthEvent` contracts. No Android/Ktor deps.
- `:core:data` — implements domain contracts. Contains the generated API clients, Kraft mappers, repositories, token storage, Koin module.
- `:composeApp` — shared Compose UI + wiring, consumed by all three platforms. Desktop `main` lives here.
- `:androidApp` — thin Android application shell (manifest, network security config, launcher).
- `build-logic/` — included build with convention plugins; all module build scripts are thin and apply `libs.plugins.convention.*` aliases. Library modules build klibs only; `:composeApp` is the **single** producer of the iOS framework (re-adding `binaries.framework` to a library plugin regresses this).

### Generated API clients (kmpgen)

`:core:data` runs kmpgen over two OpenAPI specs in `core/data/openapi/`: the game API (avalon-spring) and the auth API (ForwardAuth), generating into `hu.blu3berry.avalon.core.data.generated.{game,auth}`. Each spec produces a singleton `Api : ApiHolder()` object whose base URL and `HttpClient` are set at startup by the eager `ApiConfigurator` in `CoreDataModule.kt` — the DI graph must load before any API call.

kmpgen behaviors that shape the surrounding code:

- `eitherRequest` folds any non-2xx into `Either.Left` **before** response validators run — so 401 handling lives in `network/EitherToResult.kt` (the Either→`Result` bridge every data source uses), not in a Ktor plugin.
- The generated `Auth` interface is empty (specs declare no security scheme) — the bearer token is instead appended per-request by the pipeline interceptor in `network/HttpClientFactory.kt`.
- Generated sources land in `build/generated/kmpgen`; never edit or test them directly.

### Mappers (Kraft KSP)

DTO ↔ domain mapping uses the Kraft annotation processor (`@MapConfig`/`@MapEnum`/`@MapReverse`/`@MapUsing`), with side aliases configured in `core/data/build.gradle.kts` so call sites read `dto.toDomain()` / `domain.toDto()`. **Test only hand-written `@MapUsing` bodies** — never the declarative mappings; those are the library's responsibility.

### Dependency injection (Koin compiler plugin)

The graph is validated at compile time by the Koin compiler plugin (`io.insert-koin.compiler.plugin`, applied in `:core:data` and `:composeApp`; needs Kotlin 2.3.20+). `compileSafety` resolves every annotated definition **and** every `koinInject<T>()` / `koinViewModel()` call site — a missing binding is a `KOIN-D001`/`KOIN-D002` build error, not a crash on first navigation.

- `AvalonApp` (`:composeApp`) is the `@KoinApplication`: `@ComponentScan` picks up `@KoinViewModel`s, `includes = [CoreDataModule::class]` pulls the data graph across the module boundary.
- Startup is the typed API — `startKoin<AvalonApp>` from `org.koin.plugin.module.dsl`, **not** `org.koin.core.context.startKoin`.
- `platformCoreDataModule` is the only remaining DSL module: `expect`/`actual` bindings annotations cannot express. Its two types (`SecureSettingsFactory`, `HttpClientEngine`) are consumed with `@Provided`, which is the supported way to say "declared elsewhere" — do not reach for it to silence an ordinary missing binding.
- This supersedes decision D7 for `:core:data`'s own definitions: they are `@Single` functions on `CoreDataModule` now, because the plugin can only check definitions it can introspect (a `single { ... }` lambda body is opaque to it).

### Session / auth flow

`TokenStorage` (expect/actual: EncryptedSharedPreferences on Android, multiplatform-settings elsewhere) feeds the `HttpClientFactory` interceptor. On a 401, `EitherToResult` maps to `DataError.Network.UNAUTHORIZED` and `SessionManagerImpl` emits `AuthEvent.SessionExpired` (suspending `emit`, not `tryEmit`). Polling flows (`GameRepositoryImpl.observeGameInfo`) terminate on UNAUTHORIZED instead of re-polling.

### Testing conventions

Common tests run via the desktop target (`desktopTest`). HTTP is faked with Ktor `MockEngine`, flows asserted with Turbine. Note the process-wide hazard: tests that configure the singleton `GameApi` mutate global state — a known issue tracked as a Phase 3 prerequisite.
