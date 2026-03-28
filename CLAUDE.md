# Dotto - Development Guidelines

## Product Philosophy
Dotto is a **minimalist daily habit tracker**. Every day, a new dot.

**Core belief**: The hardest part of building a habit is showing up. Dotto removes every possible excuse not to.

### What Dotto IS:
- A one-tap daily check-in tool
- Encouraging, warm, never punishing
- Beautiful but simple — every screen earnable in under 3 seconds
- Local-first, private, zero permissions where possible

### What Dotto is NOT:
- Not a goal tracker (no targets, no percentages)
- Not a social app (no sharing, no leaderboards)
- Not an analytics platform (stats are for personal reflection, not optimization)
- Not a productivity suite (no categories, no tags, no sub-tasks)

**When in doubt, don't add it.** Every feature must pass: "Does this help the user show up today?"

## UX Principles
1. **No shame, only encouragement.** Streak = 0 says "Today is a fresh start", never "You broke your streak". Copy must always assume the user is trying their best.
2. **One tap, done.** The critical path (daily check-in) must be achievable in one tap from the home screen (or widget).
3. **Forgiveness built in.** Retroactive check-in is a feature, not a cheat. Life happens.
4. **Celebrate milestones.** 7 days, 30 days, 100 days — these moments deserve animation and delight.
5. **Respect the user's eyes.** Dark mode, proper contrast, consistent spacing. Every screen must look good in both themes.

## Tech Stack
- Kotlin + Jetpack Compose + Material 3
- Room (KSP) for local persistence
- Single Activity + Compose Navigation, MVVM
- Manual DI via Application class (no Hilt)
- minSdk 26, targetSdk 34
- WorkManager for scheduled notifications
- Glance for home screen widget

## Architecture

### Layer Structure
```
data/
  local/entity/     — Room entities, pure data classes
  local/dao/        — Room DAOs, only database queries
  repository/       — Business logic boundary (streak, toggle, stats)
ui/
  <feature>/        — Screen + ViewModel + components/ per feature
  components/       — Shared composables across features
  theme/            — Colors, Typography, Theme (light + dark)
notification/       — Reminder scheduling (WorkManager)
widget/             — Glance widget
```

- **No "domain" or "usecase" layer.** Repository is the business logic boundary.
- **No god files.** If a file exceeds ~150 lines, split it.
- **New features get their own package**, not bolted onto existing ones.

### State Management
- `StateFlow` over `LiveData`, always
- Immutable data classes for UI state
- `flatMapLatest` for reactive state-driven subscriptions (e.g., month navigation)
- Never launch unbounded `collect` coroutines — one collector per data source

### Data Handling
- `LocalDate` (java.time) for all date logic, never raw strings
- Repository methods return `Flow<T>` for observable data
- Batch related DB queries into one method (e.g., `getStats()` does one pass)
- Validate inputs at the Repository boundary with `require()`

## TDD
- **Write tests first** for business logic (Repository, ViewModel)
- **Shared test fakes** live in `testutil/FakeDaos.kt` — never duplicate
- **What deserves tests**: DAO queries, Repository logic, ViewModel state transitions
- **What does NOT**: Composable layout, navigation wiring, theme/color definitions, entity data classes
- Run `./gradlew test` after each logical unit of change

## UI Rules
- **Accessibility is mandatory.** Every interactive element must have `contentDescription`. Color picker names colors. Calendar dates include context.
- **Dark mode is mandatory.** Every new color must be defined in both light and dark schemes in `theme/Color.kt`.
- **Animations use spring(), not tween().** Spring with `DampingRatioMediumBouncy` is the app's signature feel. Fixed-duration animations feel mechanical.
- **Haptic feedback on all taps.** Check-in, calendar dates, long-press — users must feel the app respond.
- **Touch targets ≥ 48dp.** Never let animations shrink the hit area.
- **Centralize magic values** in `theme/Color.kt` (alphas, shared constants).

## Patterns & Anti-Patterns

### DO:
- Use explicit mode parameters for dual-purpose components (`isEditMode: Boolean`)
- Use `flatMapLatest` when a StateFlow drives which data to observe
- Truncate user-input text with `maxLines = 1` + `TextOverflow.Ellipsis`
- Trim input before validation (`name.trim().isNotEmpty()`)

### DON'T:
- Don't create redundant refresh paths alongside Flows
- Don't hide suspend/DB calls in functions that look pure (e.g., `toUiModel()`)
- Don't launch new `collect` coroutines on each state change
- Don't hardcode secrets or passwords — use environment variables
- Don't add features that require explanation — if it needs a tutorial, simplify it

## Build & Release

```bash
# Development
./gradlew assembleDebug          # Build debug APK
./gradlew test                   # Run unit tests

# Release (requires env vars: KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD)
KEYSTORE_PASSWORD=xxx KEY_ALIAS=xxx KEY_PASSWORD=xxx ./gradlew assembleRelease
```

### Versioning
- Semantic versioning: `MAJOR.MINOR.PATCH` (currently 0.x.y)
- `versionCode` increments with every release (integer, monotonic)
- `versionName` matches the git tag (e.g., `v0.0.3` → `"0.0.3"`)

### Release Process
1. Update `versionCode` and `versionName` in `app/build.gradle.kts`
2. Update `CHANGELOG.md` with new version section
3. Commit, tag `vX.Y.Z`, push — GitHub Actions builds and publishes the release
4. Release APK is signed with the project keystore via GitHub Secrets
5. R8 minification and resource shrinking are enabled for release builds
