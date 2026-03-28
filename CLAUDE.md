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

## Build & Verify
```bash
./gradlew assembleDebug   # Build APK
./gradlew test            # Run unit tests
```
