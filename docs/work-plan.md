# KMP migration — remaining work plan

Status legend: `[ ]` pending, `[x]` done. Tick boxes as phases land; this file is the
scheduling source of truth for the automated work sessions.

Done so far: Phase 0 (module scaffold, convention plugins) and Phase 1 (data layer:
generated API clients, Kraft mappers, repositories, token storage, session/auth flow,
desktop-target tests) — both merged to `main`. `composeApp` is still the Phase 0
placeholder and Koin wires no modules.

## Phases

- [ ] **Phase 2 — Presentation foundation + auth**
  - Wire `coreDataModule` into `initKoin` and each platform entry point.
  - MVI scaffolding (State/Action/Event base classes). Mirror rule applies: this shape
    mirrors Re-Claw; if the Re-Claw reference is unreachable from the session, implement
    the standard shape and flag it for upstream sync in the PR description.
  - Navigation host + app theme.
  - Login / Register screens against `AuthRepository`; `AuthEvent.SessionExpired`
    collector navigates back to login.
  - Verify: `./gradlew :core:data:desktopTest :core:domain:desktopTest` and a desktop
    build of `:composeApp` (see docs/dev-env-gotchas.md before debugging failures).

- [ ] **Phase 3 — Lobby**
  - Prerequisite (tracked in CLAUDE.md): remove the process-global `GameApi` singleton
    hazard in tests so screen-level tests can fake HTTP safely.
  - Home screen: create lobby (shows join code) / join by code.
  - Lobby screen: player list (poll `getPlayerNames`), settings view/edit
    (`LobbySettings`), leave, start → navigate to game.

- [ ] **Phase 4 — Game**
  - Game screen driven by `observeGameInfo` flow: phase-dependent UI for role reveal
    (`getCharacter`), king's team selection, team vote, adventure vote, assassin's
    Merlin guess, and the game outcome.

- [ ] **Phase 5 — Profile + platform finish**
  - Profile screen: view/update account, logout, delete account.
  - Platform sweep: `:androidApp:assembleRelease` (deny-all network config path),
    iOS framework build from `:composeApp`, desktop `run` smoke.

## Working agreement for scheduled sessions

One phase per session. Develop on `claude/work-planning-scheduling-tzp70g`, run the
verify commands, commit with a descriptive message, push, tick the phase's box in this
file in the same commit. When the last box is ticked, delete the daily trigger and
report completion.
