# Dotto

A minimalist daily habit tracker for Android. Every day, a new dot.

<p align="center">
  <strong>Add a habit → Tap to check in → Watch your streak grow</strong>
</p>

## Why Dotto?

Most habit trackers try to do too much. Dotto does one thing: help you show up every day. No goals, no charts, no social features. Just a daily tap and a growing streak.

- **Zero friction** — open the app, tap the dot, done
- **Streak counter** — see how many days you've kept going
- **Calendar view** — look back at your month and feel good
- **Retroactive check-in** — forgot yesterday? You can still mark it
- **No guilt** — streak breaks say "Today is a fresh start", not "You failed"

## Screenshots

*Coming soon — install and see for yourself.*

## Build

Requires JDK 17 and Android SDK (API 34).

```bash
# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# APK output
ls app/build/outputs/apk/debug/app-debug.apk
```

## Install

```bash
# Connect your Android phone via USB, then:
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Room for local storage
- Single Activity + Compose Navigation
- MVVM with StateFlow
- ~1500 lines of source code, 18 unit tests

## License

MIT
