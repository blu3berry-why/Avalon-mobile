# Post-migration work plan

Status legend: `[ ]` pending, `[x]` done. Tick boxes as phases land; this file is the
scheduling source of truth for the automated work sessions.

The KMP migration itself is finished: Phase 0–1 (module scaffold, convention plugins, data
layer) are on `main`; Phases 2–5 (presentation foundation, auth, lobby, game, profile) landed
on `claude/work-planning-scheduling-tzp70g` and are proposed in PR #6. The previous plan's
history is in git — this file replaces it.

Baseline at the time of writing (verified, not assumed): `:core:domain:desktopTest
:core:data:desktopTest :composeApp:desktopTest :androidApp:assembleDebug` is **green** —
35 tests, 0 failures, `BUILD SUCCESSFUL`. `:core:domain` contributes no tests of its own; its
`Result`/`ResultExt` are mirrored from Re-Claw and are covered upstream.

What the survey of the migrated app found, and what these phases are aimed at:

- **No CI at all.** `.github/workflows/` does not exist, so every verification is manual and
  nothing stops a red commit from landing.
- **The data layer is the untested half.** ViewModels are covered (28 tests across the five
  screens), but `AuthRepositoryImpl`, `LobbyRepositoryImpl`, `UserRepositoryImpl`,
  `SecureSettingsTokenStorage`, the `EitherToResult` status-code table and the
  `HttpClientFactory` bearer interceptor have zero tests. Only `GameRepositoryImpl` (5) and the
  hand-written mappers (2) are exercised.
- **Errors are dead ends.** Every screen renders `state.error` as a `Text` that never dismisses
  and offers no retry. `ProfileViewModel` leaves `isLoading = true` forever when there is no
  stored username. `App()` renders nothing while the token check runs.
- **The app has no identity.** No launcher icon (Android's default robot), no app theme
  resource — the manifest hardcodes `@android:style/Theme.Material.Light.NoActionBar`, so the
  system window is light even in dark mode. The pre-migration `app/` shipped `ic_morgana`, a
  Cinzel font and per-role art; none of it was carried over.
- **A release build cannot reach a server.** Both BuildKonfig flavors resolve to
  `http://localhost:…` (staging is an explicit placeholder), there is no production flavor, the
  release variant is unsigned with `isMinifyEnabled = false`, and the release network security
  config is deny-all cleartext — so a release APK on a real device has no reachable backend.
- **iOS is not runnable.** `MainViewController()` exists and the iOS klibs compile, but there is
  no `iosApp/` Xcode project and no Swift entry point, so nothing can host the framework.
- **Deferred by the migration sessions** (from the old plan's Session log): the `MviViewModel`
  scaffolding was written without the Re-Claw reference and is flagged for upstream sync, and
  the iOS framework *link* step was never run (needs a macOS host).

## Working agreement for scheduled sessions

Sessions fire overnight and are greedy: work through phases **in order** and complete as many
as possible in one session, but never at the cost of quality. Rules:

- Start by fetching this branch's head and building on it — never discard commits already there.
- A phase counts as done only when **its verify commands pass**. Tick its box in this file in
  the same commit that completes it, then push before starting the next phase.
- Never push a half-finished phase. If a phase can't be completed cleanly (failing verification,
  missing reference, context running low), stop after the last pushed green phase and leave a
  short note under "Session log" instead.
- `export LC_ALL=C.UTF-8` before any gradle command. Read `docs/dev-env-gotchas.md` before
  debugging a build failure — the pre-existing KLIB duplicate-`unique_name` warnings are noise,
  and `./gradlew --version` succeeding proves nothing about whether gradle can build here.
- Mirrored infrastructure rules in `CLAUDE.md` still apply: `core/domain/.../result/`,
  `build-logic/convention/` and the MVI scaffolding mirror Re-Claw. Improvements go upstream
  first; bugs are fixed in both repos immediately.
- When the last box is ticked, delete the overnight trigger and report completion.

## Phases

- [ ] **Phase 1 — CI: a green-build gate**

  *Goal:* every later phase's verification runs itself on every push, so "it passed" stops
  being a claim a session has to be trusted on.

  - Add `.github/workflows/ci.yml`: trigger on `push` and `pull_request`, JDK 17
    (`projectJavaVersion`), `actions/setup-java` with the gradle cache enabled.
  - Job `test`: `:core:domain:desktopTest :core:data:desktopTest :composeApp:desktopTest`.
  - Job `android`: `:androidApp:assembleDebug :androidApp:assembleRelease` (the release variant
    is what exercises the deny-all network security config).
  - Job `ios-klib`: `:composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64`
    on the Linux runner. Framework *linking* stays out of CI until a macOS runner is justified —
    say so in a comment in the workflow rather than silently omitting it.
  - Upload `**/build/test-results/**/*.xml` as an artifact on failure.
  - Add a `docs/ci.md` (or a `## CI` section in `CLAUDE.md`) naming which job covers what, so a
    future session knows which check a change should have moved.

  VERIFY COMMANDS:
  ```bash
  export LC_ALL=C.UTF-8
  ./gradlew :core:domain:desktopTest :core:data:desktopTest :composeApp:desktopTest
  ./gradlew :androidApp:assembleDebug :androidApp:assembleRelease
  ./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64
  ```
  Plus: the workflow must be green on this branch's PR before the box is ticked — a workflow
  file that has never run is not a verified phase.

- [ ] **Phase 2 — Data-layer test coverage**

  *Goal:* close the real hole in the test suite. The ViewModels are covered; the repositories
  underneath them are not.

  - `AuthRepositoryImplTest`: login persists token **and** username; register does not; a 401
    from `/login` maps to `UNAUTHORIZED` **without** emitting `AuthEvent.SessionExpired` (the
    documented auth-service carve-out in `EitherToResult.kt`); `logout()` clears storage and
    emits `LogoutRequired`.
  - `LobbyRepositoryImplTest`: `create` returns the join code; `getSettings`/`updateSettings`
    round-trip a `LobbySettings`; `getPlayerNames` maps the list; a 401 on any of them raises
    `SessionExpired` exactly once.
  - `UserRepositoryImplTest`: `get`/`update`/`delete`, including that a blank password is sent
    as an omitted field (`explicitNulls = false` in `AvalonJson` is what makes that work — assert
    on the request body, not just the response).
  - `EitherToResultTest`: the status-code table (400/401/403/404/408/409/413/429/500/503/other)
    and the three exception branches (`IOCallException` → `NO_INTERNET`,
    `SerializationException` → `SERIALIZATION`, `UnexpectedCallException` → `UNKNOWN`).
  - `HttpClientFactoryTest`: the pipeline interceptor appends `Authorization: Bearer …` when a
    token exists, sends no header when it doesn't, and — the case the `remove` call in that file
    exists for — does not emit two `Authorization` headers when the same request builder runs
    through the pipeline twice.
  - `SecureSettingsTokenStorageTest` on the desktop target: save / read / `hasToken` / `clear`.
  - Use the existing `TestApiSupport.useGameApiEngine(...)` / `jsonEngine(...)` for anything
    touching the game API; per `CLAUDE.md` the generated `Api` singletons are process-wide and
    only `:core:data` tests may mutate them. The auth repository needs the same treatment for
    `AuthApi` — add a sibling helper in `TestApiSupport.kt` rather than re-pointing the
    singleton from a test body. The interceptor test is the exception: it should call
    `createHttpClient(...)` directly with its own `TokenStorage`, since `useGameApiEngine`
    hardcodes an `InMemoryTokenStorage` and would hide exactly what that test is asserting.

  VERIFY COMMANDS:
  ```bash
  export LC_ALL=C.UTF-8
  ./gradlew :core:data:desktopTest :core:domain:desktopTest
  ```

- [ ] **Phase 3 — Error and loading states, and an app identity**

  *Goal:* make failure recoverable and the app look like Avalon rather than a default project.

  Error / loading:
  - Give each screen a `Scaffold` with a `SnackbarHost`, and surface `state.error` through it
    (dismissible, auto-expiring) instead of the permanent inline `Text` used today. Keep the
    two message mappers as they are — `AuthUi.toAuthMessage()` for auth, `ui.toUserMessage()`
    elsewhere; the split is deliberate (a 401 on login means "wrong password").
  - Add a retry affordance where a retry is meaningful: Home (create/join), Lobby (first
    settings fetch), Profile (initial load). A dropped poll needs no button — the next tick
    already retries.
  - Fix `ProfileViewModel`: the `username == null` branch returns without clearing
    `isLoading`, leaving a permanent spinner if the logout event doesn't navigate away.
  - Fix `App()`'s start-up flash: `produceState` returns `null` for the first frame and the
    composable returns nothing, so the window is blank until the token read completes. Render a
    real splash/loading state, and decide what happens if `isLoggedIn()` throws.
  - `LobbyScreen` has no loading state before the first player list lands, and `Start game` is
    enabled for every player at every player count. Show the loading state; gate Start on what
    the server actually accepts (and surface the rejection when it doesn't).
  - `GameScreen` has no way out before `OUTCOME` — add a leave/exit path with confirmation.

  Identity:
  - Adaptive launcher icon + `android:label`, and an app theme resource so the system window
    follows dark mode (the manifest currently pins `Theme.Material.Light.NoActionBar` while
    `AvalonTheme` already handles light/dark for the Compose content).
  - Carry over what the pre-migration `app/` had and the rewrite dropped: the Cinzel display
    font and the per-role art in `app/src/main/res/drawable/` (`merlin_c.png`,
    `morgana_c.png`, `percival_placeholder_c.png`, …). Put shared assets in `composeApp`'s
    `commonMain/composeResources` so all three platforms get them, not in `androidApp`.
    `app/` is legacy and read-only — copy the asset files out, don't wire the module in.
  - Use the art in `GameScreen`'s role reveal, which today prints the enum name with underscores
    replaced.

  VERIFY COMMANDS:
  ```bash
  export LC_ALL=C.UTF-8
  ./gradlew :composeApp:desktopTest
  ./gradlew :androidApp:assembleDebug :androidApp:assembleRelease
  xvfb-run -a ./gradlew :composeApp:run -Dskiko.renderApi=SOFTWARE_COMPAT   # smoke, then close
  ```

- [ ] **Phase 4 — Release configuration**

  *Goal:* make a build that a person can install and actually use.

  - Add a `production` flavor to `BuildKonfigConventionPlugin` alongside `dev`/`staging`, and
    replace the placeholder staging URLs. Production must be `https://` — the release network
    security config is deny-all cleartext, so an `http://` production URL fails at runtime, not
    at build time. If the real deployment URLs aren't known in the session, stop and ask rather
    than inventing hostnames: this is the one thing in this plan that cannot be guessed.
  - Release signing: a `signingConfigs` block in `AndroidApplicationConventionPlugin` reading
    from `keystore.properties` / environment variables, falling back to unsigned when absent so
    a contributor without the keystore can still build. Never commit a keystore or its password;
    add `keystore.properties` to `.gitignore`.
  - Turn on `isMinifyEnabled` for release with a `proguard-rules.pro` covering the kotlinx
    serialization, Ktor and Koin reflective surfaces, and verify the **minified release** app
    still logs in — an R8 build that assembles but crashes on first request is the classic
    failure here.
  - Versioning: `projectVersionCode` / `projectVersionName` / `desktopPackageVersion` currently
    say `1` / `0.1.0` / `0.1.0`. Agree a scheme, write it down, and add a `CHANGELOG.md`.
  - Extend the CI workflow from Phase 1 with a tag-triggered job that builds the release
    artifacts.

  VERIFY COMMANDS:
  ```bash
  export LC_ALL=C.UTF-8
  ./gradlew :androidApp:assembleRelease -Pflavor=production
  ./gradlew :androidApp:bundleRelease -Pflavor=production
  ./gradlew :composeApp:packageDistributionForCurrentOS
  ```
  Plus: confirm the minified release APK reaches the backend (an emulator or device run against
  a live server) — assembling is not evidence that R8 kept what Ktor and kotlinx.serialization
  need at runtime.

- [ ] **Phase 5 — iOS app shell**

  *Goal:* iOS stops being a target that only compiles and becomes a target that runs.

  - Add `iosApp/` : an Xcode project with a Swift `@main` App that hosts
    `MainViewController()` from the `ComposeApp` framework, plus `Info.plist`, a bundle ID
    matching `projectApplicationId`, and an app icon set.
  - Wire the framework: either the standard `embedAndSignAppleFrameworkForXcode` build phase or
    a `Podfile`/SPM setup — pick one and document it in `CLAUDE.md`; `:composeApp` stays the
    single framework producer (re-adding `binaries.framework` to a library convention plugin
    regresses this).
  - `TokenStorage`'s iOS actual uses multiplatform-settings, not the Keychain. Decide whether
    that is acceptable for a release build and either switch it or write down why not.
  - **This phase has a hard ceiling on Linux.** A session on this host can add the project files
    and verify the klibs compile, but `linkDebugFrameworkIosArm64` / `linkReleaseFrameworkIosArm64`
    and anything Xcode requires a macOS host. Do the part that is possible, verify it, and record
    the remaining macOS-only steps in the Session log rather than ticking the box on a build that
    was never linked.

  VERIFY COMMANDS:
  ```bash
  export LC_ALL=C.UTF-8
  ./gradlew :composeApp:compileKotlinIosArm64 :composeApp:compileKotlinIosSimulatorArm64
  ```
  On macOS (required to tick this box):
  ```bash
  ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
  xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp \
    -destination 'platform=iOS Simulator,name=iPhone 15' build
  ```

- [ ] **Phase 6 — Compose UI tests, and removing the legacy app**

  *Goal:* lock in the Phase 3 UI work, and drop the pre-migration code now that nothing
  references it.

  - Wire `compose.uiTest` (`@OptIn(ExperimentalTestApi::class)`, `runComposeUiTest`) into
    `:composeApp`'s `commonTest` / desktop test target.
  - One smoke test per screen against faked repositories — reuse the existing `LobbyFakes.kt`
    pattern rather than inventing a second fake style: renders, shows its loading state, shows
    an error with a working retry, and completes its primary action.
  - A navigation test over `AvalonNavHost`: logged-out start lands on Login; an
    `AuthEvent.SessionExpired` emission from anywhere routes back to Login with the banner set.
  - Delete `app/` and `testing/` (the pre-migration Android app and scripts) once Phase 3 has
    taken the assets it needs. Neither is in `settings.gradle.kts`; git history keeps them.
    Update `CLAUDE.md` to drop the "legacy, do not touch" section in the same commit.
  - Sync `MviViewModel` against the Re-Claw reference if it is reachable from the session — the
    migration session wrote it blind and flagged it. If Re-Claw is still unreachable, say so in
    the Session log and leave the file alone.

  VERIFY COMMANDS:
  ```bash
  export LC_ALL=C.UTF-8
  ./gradlew :composeApp:desktopTest
  ./gradlew :core:data:desktopTest :core:domain:desktopTest
  ./gradlew :androidApp:assembleDebug :androidApp:assembleRelease
  ```

## Session log

(Overnight sessions append blockers/carry-over notes here.)

## How to schedule

Overnight sessions must run as **persistent sessions with the repository already attached**.

The 2026-08-14..18 nightly fires produced nothing: each firing created a *fresh* session, and
the fresh-session `add_repo` pattern does not give the session push credentials — so five nights
of work were lost. Binding the trigger to a persistent session that already has the repo
attached is what made the 2026-08-19 session (Phases 2–5) work.

So, when scheduling:

- Create the trigger with `persistent_session_id` pointing at a session that has
  `blu3berry-why/Avalon-mobile` attached with push access.
- Do **not** use `create_new_session_on_fire` for this work.
- Write the trigger prompt assuming continuing context ("continue the work plan"), not as a
  standalone instruction — a persistent session resumes its conversation.
- Delete the trigger once the last box is ticked.
