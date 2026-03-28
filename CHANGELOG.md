# Changelog

All notable changes to Dotto will be documented in this file.

Format follows [Keep a Changelog](https://keepachangelog.com/).

## [0.1.0] - 2026-03-29

### Added
- Daily reminder notifications per habit (WorkManager-based, skips if already checked in)
- Milestone celebrations: full-screen overlay with spring animation at 7/30/100/365 day streaks
- Home screen widget (Glance): shows top 5 habits with check status and streak count
- Annual heatmap on detail page: GitHub-style contribution grid for the current year
- Reminder indicator card on detail page when a reminder is set
- Bell icon in detail top bar to set/clear reminders
- Database migration v1→v2 for reminder fields (reminderHour, reminderMinute)

### Changed
- CLAUDE.md rewritten with product philosophy, UX principles, and expanded architecture guidelines
- App icon updated to 3×3 calendar dot grid design

## [0.0.3] - 2026-03-29

### Added
- Dark mode support (follows system theme)
- Loading indicator on home screen
- Accessibility: content descriptions for all interactive elements (check buttons, calendar dates, color picker)
- Calendar date taps now have haptic feedback
- Detail screen shows encouraging messages for all streak states (fresh start / personal best / keep going)

### Fixed
- Check button touch target no longer shrinks below 48dp during scale animation
- Long habit names now truncate with ellipsis instead of breaking layout
- Color picker selection effect made more prominent (larger scale, border, shadow, checkmark)
- Future date contrast improved (alpha 0.3 → 0.5) for better readability
- Whitespace-only habit names no longer pass validation
- FAB now always visible (was hidden when habit list was empty)
- Delete dialog text simplified for small screens
- Removed hardcoded keystore fallback passwords from build config

### Changed
- Enabled R8 code shrinking and resource optimization for release builds
- Disabled automatic backup (android:allowBackup=false) for privacy

## [0.0.2] - 2026-03-29

### Fixed
- Release APK now uses proper release signing (was previously unsigned, causing "package info empty" on install)
- Release workflow uses GitHub Secrets for keystore management

## [0.0.1] - 2026-03-28

### Added
- Core habit management: create, edit, delete habits with custom colors
- Daily check-in with animated tap and haptic feedback
- Streak counter (current streak displayed on each habit card)
- Detail screen with monthly calendar view
- Retroactive check-in: tap past dates in the calendar to mark them
- Stats: current streak, longest streak, total check-ins
- Empty state onboarding: "Small steps, big changes"
- Long-press to delete habits from home screen
- Encouraging UX: "Today is a fresh start" when streak is 0
- 18 unit tests covering Repository and ViewModel logic
- CLAUDE.md with development guidelines and anti-patterns
