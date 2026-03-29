# Changelog

All notable changes to Dotto will be documented in this file.

Format follows [Keep a Changelog](https://keepachangelog.com/).

## [Unreleased]

### Changed
- Prepare Release workflow now auto-triggers Release workflow (no manual second step needed)
- Release workflow supports manual dispatch via workflow_dispatch
- CHANGELOG uses [Unreleased] pattern: PR authors no longer need to guess version numbers

## [0.1.3] - 2026-03-29

### Added
- Check-in bottom sheet: tapping check button now opens a modal with check-in toggle and comment input
- Habit note field: add a personal note when creating or editing a habit (stored on the habit itself)
- Calendar check-in: tap any past date in the detail screen calendar to check in or add a comment via the same modal
- Boot receiver: reminders now survive device reboots (BOOT_COMPLETED broadcast)
- App-launch reminder recovery: all reminders are rescheduled when the app starts

### Changed
- Check-in comment moved from inline text field on home card to the new check-in bottom sheet
- Home card shows comment as read-only preview (single line, ellipsis)
- Calendar long-click no longer restricted to checked dates only
- Reminder scheduling uses CANCEL_AND_REENQUEUE policy so time changes take effect immediately
- Database migration v3→v4 for habit note column

### Fixed
- Notification reminders silently disappearing after device reboot or app update
- Changing reminder time not taking effect until next cycle (was using UPDATE policy)

## [0.1.2] - 2026-03-29

### Added
- Manual theme switching: cycle between System / Light / Dark via top bar icon
- Widget one-tap check-in: tap the check mark on any habit row to toggle directly from widget
- Notification quick check-in: "Check in ✓" action button on reminder notifications
- Confetti animation on check-in (6-particle burst with spring physics)
- Optional check-in comment (50 chars max): inline note field on home screen, long-press calendar date to edit in detail screen
- Comment indicator dots on calendar dates that have notes
- Data export/import (JSON via SAF): backup and restore all habits and check-in history
- Database migration v2→v3 for check-in comment field

### Changed
- Color picker selected border: adaptive color instead of hard black (darkened for light colors, white for dark colors)
- Version bumped to 0.1.2 (versionCode=6)

## [0.1.1] - 2026-03-29

### Added
- Long press habit card to enter edit mode: rename inline, delete button replaces check
- Smooth horizontal slide navigation transitions (spring curve)
- Dark mode window background (#121212) to prevent white flash on page transitions

### Fixed
- App crash on launch: Compose BOM 2024.01.00 → 2024.02.02 (KeyframesSpec.at() NoSuchMethodError)
- Color picker selection never showing as selected (Color.value.toInt() → Color.toArgb())
- Edit mode exiting immediately after long press (onFocusChanged initial false trigger)
- System back press and tap-outside now save and exit edit mode
- FAB hidden when habit list is empty to avoid duplicate add buttons

### Changed
- App icon simplified to single dot + checkmark
- Version bumped to 0.1.1 (versionCode=5)

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
