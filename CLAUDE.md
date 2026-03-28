# Dotto - Development Guidelines

## Project Overview
Dotto is a minimalist daily habit tracker Android app. Every day, a new dot.

## Tech Stack
- Kotlin + Jetpack Compose + Material 3
- Room (KSP) for local persistence
- Single Activity + Compose Navigation, MVVM
- Manual DI via Application class (no Hilt)
- minSdk 26, targetSdk 34

## TDD Principles
- **Write tests first** for business logic (Repository, ViewModel). Write the test, watch it fail, then implement.
- **Don't test for the sake of testing.** No tests for trivial getters/setters, theme constants, or pure UI composables with no logic.
- **What deserves tests:**
  - DAO queries (using Room in-memory database)
  - Repository logic (toggle, streak calculation, data transformations)
  - ViewModel state transitions (loading → loaded, toggle behavior, edge cases)
- **What does NOT need tests:**
  - Composable layout/rendering (trust Compose)
  - Navigation wiring
  - Theme/Color/Typography definitions
  - Entity data class definitions
- **Shared test fakes live in `testutil/FakeDaos.kt`.** Never duplicate fake DAO implementations across test files.
- Run `./gradlew test` after each logical unit of change.

## Module & Layer Structure
- **Keep layers thin.** Each file should have one clear responsibility.
- **Data layer** (`data/`):
  - `local/entity/` — Room entities, pure data classes, no logic
  - `local/dao/` — Room DAOs, only database queries
  - `repository/` — Single source of truth for business data access. All business logic (streak calculation, toggle) lives here.
- **UI layer** (`ui/`):
  - `<feature>/` — Each feature gets its own package (home, detail)
  - `<feature>/components/` — Composables scoped to that feature
  - `<feature>/<Feature>ViewModel.kt` — Holds UI state, delegates to Repository
  - `<feature>/<Feature>Screen.kt` — Top-level screen composable
  - `components/` — Shared UI components used across features
  - `theme/` — Material 3 theme, colors, typography
- **No "domain" or "usecase" layer.** The app is too small. Repository is the business logic boundary.
- **No god files.** If a file exceeds ~150 lines, consider splitting.

## Code Style
- Prefer `StateFlow` over `LiveData`
- Prefer immutable data classes for UI state
- Use `LocalDate` (java.time) for date handling, never raw strings in business logic
- Repository methods should return `Flow<T>` for observable data
- ViewModel exposes `StateFlow<UiState>` to the UI

## Patterns & Anti-Patterns (Learned from Code Review)

### DO:
- **Batch related DB queries into one method.** Use `getStats()` instead of calling `calculateCurrentStreak()`, `calculateLongestStreak()`, `totalCheckIns()` separately. One DB read, one pass.
- **Use `flatMapLatest` for reactive state-driven subscriptions.** When a StateFlow drives which data to observe (e.g., current month), use `flatMapLatest` to auto-cancel the previous subscription. Never launch a new `collect` coroutine on each state change.
- **Use explicit mode parameters for dual-purpose UI components.** If a composable serves both "create" and "edit" modes, pass an explicit `isEditMode: Boolean` — don't infer mode from data values.
- **Centralize magic values in `theme/Color.kt`.** All shared color constants, alpha values, and UI constants belong there.
- **Validate inputs at the Repository boundary.** Use `require()` for preconditions like non-blank names.

### DON'T:
- **Don't create redundant refresh paths.** If a Flow already observes data changes, don't add a manual `refreshXxx()` method that reads stale state and overwrites. One source of truth.
- **Don't hide suspend/DB calls in extension functions that look pure.** A function named `toUiModel()` should not make database calls. Make the data-fetching explicit.
- **Don't launch unbounded `collect` coroutines.** Every `launch { flow.collect {} }` lives forever in its scope. If you call it multiple times (e.g., on month change), you get parallel collectors fighting over state.

## Build & Verify
```bash
./gradlew assembleDebug   # Build APK
./gradlew test            # Run unit tests
```
