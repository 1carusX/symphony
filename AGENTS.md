# AGENTS.md

Guidance for coding agents working in this repository.

## Project

Symphony is an offline music player with two frontends:

- `:app` — the full Android application (Android 9+).
- `:desktop` — a minimal Compose Desktop application for Linux.

Shared, platform-neutral logic lives in `:core`. The desktop UI is intentionally
separate from the Android UI; do not assume the two frontends have feature parity.

Read `README.md` for product context and `docs/ROADMAP.md` before changing
dependencies, SDK levels, or long-planned architecture. For audio metadata,
artwork, or lyrics work, also read `docs/audio-metadata-invariants.md`.

## Modules

| Module       | Purpose                                                                 |
| ------------ | ----------------------------------------------------------------------- |
| `:app`       | Android UI, services, SAF scanning, Room stores, playback, MediaSession |
| `:core`      | Android/JVM shared utilities and the audio metadata contract/resolver   |
| `:desktop`   | Compose Desktop UI, filesystem library scan, VLCJ playback              |
| `:i18n`      | Compose Resources translations and pluralization                        |
| `:metaphony` | Android JNI wrapper around TagLib, built with NDK and CMake             |
| `buildSrc`   | Release/version/helper Gradle tasks                                     |

The `metaphony` TagLib checkout is a Git submodule. Initialise it before
building anything that compiles `:app` or `:metaphony`:

```bash
git submodule update --init --recursive
```

Do not modify the TagLib submodule unless the task explicitly requires it.

## Build and test

Prerequisites:

- A JDK 25 capable of running Gradle; the daemon is pinned through
  `gradle/gradle-daemon-jvm.properties`.
- Android SDK for `:app` and the NDK/CMake versions selected in the build.
- System LibVLC 3 for meaningful desktop playback tests and the desktop app:
  `libvlc5` and `vlc-plugin-base` on Debian/Ubuntu.

Common commands:

```bash
# Host tests across all modules plus the translation catalog check
./gradlew :i18n:verifyI18n :app:testDebugUnitTest :core:jvmTest :i18n:jvmTest :desktop:jvmTest

# Android debug build
./gradlew :app:assembleDebug

# Android lint (the same task CI runs)
./gradlew lintRelease

# Desktop application and tests
./gradlew :desktop:run
./gradlew :desktop:jvmTest

# Linux packages; patches the deb to declare the LibVLC runtime dependencies
./gradlew :desktop:patchDebDependencies
```

Run a focused test first when iterating, then run the relevant full suite before
declaring the work complete. Android instrumented tests require a device or
emulator and are not part of the host test command:

```bash
./gradlew :app:connectedDebugAndroidTest
./gradlew :metaphony:connectedDebugAndroidTest
```

The fixture audio files are shared from `app/src/test/resources`; `:core` also
uses that directory as test resources. Do not rename or move them without
updating both test configurations.

Desktop VLCJ integration tests skip themselves when LibVLC cannot be found.
A green `:desktop:jvmTest` without LibVLC therefore does not validate real
playback; state that explicitly when reporting verification.

## Architecture map

### Android

- `app/src/main/java/io/github/zyrouge/symphony/Symphony.kt` is the composition
  root. It owns permissions, settings, database, `Groove`, and `Radio`.
- `MainActivity` wires lifecycle hooks and sets the Compose content.
- `services/groove/MediaExposer.kt` scans SAF folders and handles audio, LRC,
  and M3U files. `Groove` owns the library repositories and scan lifecycle.
- `services/groove/Song.kt` parses metadata through `AudioMetadataSource`.
  Production dispatch prefers Metaphony and falls back to
  `MediaMetadataRetriever`.
- Repositories hold in-memory indexes and expose `StateFlow`s to Compose.
- Android playback is `services/radio/Radio.kt`, `RadioQueue`, and
  `RadioPlayer` (Android `MediaPlayer`), with audio focus, MediaSession,
  notifications, sleep timer, speed/pitch, and queue persistence.
- Persistent state is split between `SharedPreferences` (`Settings`), Room
  (`cache` and `persistent` databases), artwork files, and a SQLite lyrics
  cache.

### Shared

- `:core` has `commonMain`, `jvmSharedMain`, `androidMain`, and `jvmMain`.
  Keep Android types out of `commonMain` and `jvmSharedMain`.
- The shared metadata contract and pure resolver live in
  `core/src/jvmSharedMain/.../services/groove/AudioMetadataSource.kt`.
- JVM-only implementations such as `JaudiotaggerMetadataSource` and
  `FileSystemScanner` belong in `core/src/jvmMain`.
- Put testable logic here rather than duplicating it in the Android and
  desktop modules.

### Desktop

- Entry point: `desktop/src/jvmMain/.../desktop/Main.kt`.
- `DesktopLibrary` scans a directory through `FileSystemScanner` and
  Jaudiotagger, producing `SongFields`.
- `VlcjAudioPlayer` sits behind `AudioPlayer` and drives
  `PlaybackStateMachine`; keep UI concerns out of the player.
- Packaging currently targets Linux Deb and AppImage. Windows/macOS packaging
  and bundled LibVLC are not implemented.

### Internationalization

- User-visible strings live in
  `i18n/src/commonMain/composeResources/values*/strings.xml`.
- `Strings.kt` exposes typed getters and the `rememberStrings` map. A new
  Android UI string must be added there as well as to the resource catalogs.
- `:i18n:verifyI18n` requires every locale directory to contain every base
  string (and plural `_other` entries). Run it after changing strings.
- Do not hand-edit generated `Res` classes or files matching `*.g.kt`.

## Invariants and conventions

- **Preserve metadata differences.** Metaphony and `MediaMetadataRetriever`
  intentionally differ in ID suffix, duration/bitrate units, artwork handling,
  and lyrics caching. Do not "unify" them. `docs/audio-metadata-invariants.md`
  records the contract.
- **Keep platform boundaries explicit.** Android-only APIs stay in `:app`;
  desktop replacements stay in `:desktop`; only genuinely portable code moves
  to `:core`.
- **Room changes need migrations.** Update the entity/database version and add
  a migration; schemas are exported to `app/room-schemas`. Do not enable
  destructive migrations or drop persisted user data.
- **Dependency versions belong in `gradle/libs.versions.toml`.** Do not perform
  unrelated upgrades. Check `docs/ROADMAP.md` for frozen versions and decisions.
- **Follow the Bento design system on Android.** Use existing `Bento*`
  components, `LocalBentoColors`, spacing/radius tokens, and adaptive window
  classes. Avoid reintroducing Material container components where a Bento
  equivalent exists.
- **Match each module's test stack.** `:app`, `:core`, and `:desktop` use JUnit
  Jupiter; `:i18n` uses `kotlin.test` on the JUnit Platform. Put `:app` tests
  under `app/src/test`, `:core` tests under `core/src/jvmTest`, desktop tests
  under `desktop/src/jvmTest`, and shared i18n tests under
  `i18n/src/commonTest`.
- **Keep commits focused.** Commit messages are English, at most 72 characters
  in the title, with no AI-attribution lines.
- **Never read, print, copy, or commit `secrets/` or `local.properties`.**
  Build outputs, `dist/`, `.omo/`, caches, and generated files are ignored.

## Verification expectations

Before finishing:

1. Run the focused tests covering the change.
2. Run the shared host test command above where practical.
3. Run `:app:assembleDebug` for Android build changes and `lintRelease` for
   Android or build-policy changes.
4. For desktop UI/playback changes, run `:desktop:run` or the applicable smoke
   task when a display/audio environment is available.
5. Report any unrun Android-device, desktop packaging, or manual playback checks
   plainly rather than presenting them as verified.
