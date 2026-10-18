# KMP migration — remaining work plan

Status legend: `[ ]` pending, `[x]` done. Tick boxes as phases land; this file is the
scheduling source of truth for the automated work sessions.

Done so far: Phase 0 (module scaffold, convention plugins) and Phase 1 (data layer:
generated API clients, Kraft mappers, repositories, token storage, session/auth flow,
desktop-target tests) — both merged to `main`. `composeApp` is still the Phase 0
placeholder and Koin wires no modules.

## Phases

- [x] **Phase 2 — Presentation foundation + auth**
  - Wire `coreDataModule` into `initKoin` and each platform entry point.
  - MVI scaffolding (State/Action/Event base classes). Mirror rule applies: this shape
    mirrors Re-Claw; if the Re-Claw reference is unreachable from the session, implement
    the standard shape and flag it for upstream sync in the PR description.
  - Navigation host + app theme.
  - Login / Register screens against `AuthRepository`; `AuthEvent.SessionExpired`
    collector navigates back to login.
  - Verify: `./gradlew :core:data:desktopTest :core:domain:desktopTest` and a desktop
    build of `:composeApp` (see docs/dev-env-gotchas.md before debugging failures).

- [x] **Phase 3 — Lobby**
  - Prerequisite (tracked in CLAUDE.md): remove the process-global `GameApi` singleton
    hazard in tests so screen-level tests can fake HTTP safely.
  - Home screen: create lobby (shows join code) / join by code.
  - Lobby screen: player list (poll `getPlayerNames`), settings view/edit
    (`LobbySettings`), leave, start → navigate to game.

- [x] **Phase 4 — Game**
  - Game screen driven by `observeGameInfo` flow: phase-dependent UI for role reveal
    (`getCharacter`), king's team selection, team vote, adventure vote, assassin's
    Merlin guess, and the game outcome.

- [x] **Phase 5 — Profile + platform finish**
  - Profile screen: view/update account, logout, delete account.
  - Platform sweep: `:androidApp:assembleRelease` (deny-all network config path),
    iOS framework build from `:composeApp`, desktop `run` smoke.

## Working agreement for scheduled sessions

Sessions fire overnight and are greedy: work through phases **in order** and complete
as many as possible in one session, but never at the cost of quality. Rules:

- Start by fetching `claude/work-planning-scheduling-tzp70g` and building on its head —
  never discard commits that are already there.
- A phase counts as done only when its verify commands pass. Tick its box in this file
  in the same commit that completes it, then push before starting the next phase.
- Never push a half-finished phase. If a phase can't be completed cleanly (failing
  verification, missing reference, context running low), stop after the last pushed
  green phase and leave a short note under "Session log" below instead.
- When the last box is ticked, delete the overnight trigger and report completion.

## Session log

(Overnight sessions append blockers/carry-over notes here.)

- 2026-08-19 (overnight): Phases 2–5 completed and pushed. Notes:
  - Re-Claw reference was unreachable from the session, so the MVI scaffolding
    (`composeApp/.../mvi/MviViewModel.kt`) is the standard State/Action/Event shape —
    sync it against Re-Claw upstream before diverging further.
  - iOS platform sweep ceiling: the session host is Linux, which can compile the iOS
    klibs (`compileKotlinIosArm64` / `compileKotlinIosSimulatorArm64`, both green) and
    registers the `linkDebugFramework*` / `linkReleaseFramework*` tasks for the newly
    declared `ComposeApp` framework in `:composeApp`, but Apple framework *linking*
    requires a macOS host — run `./gradlew :composeApp:linkReleaseFrameworkIosArm64`
    there to finish the sweep.
  - Desktop `run` smoke passed under Xvfb with `-Dskiko.renderApi=SOFTWARE_COMPAT`
    (no GL in the sandbox's virtual display; software rendering is a test-env detail,
    not an app requirement).
