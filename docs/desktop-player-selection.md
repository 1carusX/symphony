# Desktop player backend selection

Scope: the `:desktop` module of this repository needs an audio playback backend for the D3
desktop shell. This document records the three candidates, what was measured on the
development machine, and the resulting decision. It is the artifact required by the D3 plan
(T2); the probe code it describes is temporary and was removed in T3.

Date: 2026-11-19
Machine: Debian 13 (trixie), kernel 6.12, PipeWire 1.x with a PulseAudio-compatible server,
JDK 25 on `PATH`, project toolchain 21 for `:desktop`.

## Decision

**vlcj 4.12.1 over the system LibVLC (`libvlc5`, 3.0.24 on this machine).**

Rationale in one line: it is the only candidate that decodes every fixture container
(mp3, flac, m4a, ogg) on the project toolchain, and on Linux the native dependency is a
distro package that a `deb` can declare instead of shipping.

The backend sits behind the `AudioPlayer` abstraction added in T3, so a later swap does not
touch the UI.

## Candidates and exact versions tested

| #   | Candidate            | Artifacts tested                                                                     | Runtime                                             |
| --- | -------------------- | ------------------------------------------------------------------------------------ | --------------------------------------------------- |
| A   | vlcj                 | `uk.co.caprica:vlcj:4.12.1` (+ `vlcj-natives:4.12.0`, JNA 5.6.0)                     | system LibVLC 3.0.24 (`libvlc5`, `vlc-plugin-base`) |
| A'  | vlcj (older pairing) | `uk.co.caprica:vlcj:3.10.1`                                                          | system LibVLC 3.0.24                                |
| B   | JavaFX Media         | `org.openjfx:javafx-{base,graphics,media}:26` with `linux` classifier                | JRE 25 (see JDK note)                               |
| C   | Java Sound SPI       | `mp3spi:1.9.5.4`, `vorbisspi:1.0.3.3`, `jflac-codec:1.5.2`, `tritonus-share:0.3.7.4` | JDK 21 (project toolchain)                          |

Candidate versions differ from the ones listed in the plan on purpose, and the difference is
itself a finding:

- The plan named vlcj 4.12.1, which was tested first. The vlcj 4 line needs LibVLC 3.0.0 or
  newer (constant `VLC_3_0_0` in `uk.co.caprica.vlcj.support.version.LibVlcVersion`), so it
  pairs with the `libvlc5` package distros ship. It works; see the matrix.
- vlcj 3.10.1 was tested as a fallback in case the 4.x line refused LibVLC 3. It also works
  but was released in 2015 and uses the flat `playMedia/getTime` API. Not chosen.
- JavaFX Media 26 was used instead of 25.x because that is the current stable line resolved
  from Maven; 25.x would have the same packaging story.

## Method

Fixtures: the six files already in this repository, `app/src/test/resources/`
(`audio.mp3`, `audio-id3v2.3.mp3`, `audio-id3v2.4.mp3`, `audio.flac`, `audio.ogg`,
`audio.m4a`).

Three throwaway probes lived in `desktop/src/jvmMain/kotlin/.../desktop/spike/` and ran as
Gradle `JavaExec` tasks with each backend on the classpath:

- `JavaSoundProbe` opens the file with `AudioSystem`, converts to 16-bit PCM, decodes the
  whole stream and opens a `SourceDataLine` for the same format.
- `JavaFxProbe` starts the JavaFX toolkit, creates a `Media`/`MediaPlayer` per file and waits
  for `onReady`, reporting `media.duration`.
- `VlcProbe` creates a media player through vlcj and plays each file, sampling
  `status().time()` every 50 ms and reporting the maximum observed time.

Pass criterion for A/B/C, per the plan: open `audio.mp3` and read a duration. Everything
beyond that is recorded here as measured coverage.

Raw logs are archived next to this document's evidence:

- `.omo/evidence/symphony-d3/spike-javasound.log`
- `.omo/evidence/symphony-d3/spike-javafx.log`
- `.omo/evidence/symphony-d3/spike-vlc-fine.log`
- `.omo/evidence/symphony-d3/spike-vlc-javasound.log` (combined early run)
- `.omo/evidence/symphony-d3/spike-vlc-javafx.log` (combined early run)

## Measured matrix

Format coverage is what the probe observed on this machine, not what the library documents.

| Fixture             | A: vlcj 4.12.1                                                              | B: JavaFX Media 26                             | C: Java Sound SPI                                                              |
| ------------------- | --------------------------------------------------------------------------- | ---------------------------------------------- | ------------------------------------------------------------------------------ |
| `audio.mp3`         | plays, time advances (`maxTimeMs=292`, `lengthMs=1080`)                     | ready, `durationMs=6072`                       | decoded 184320 B, `SourceDataLine` opens, `durationUs=2116000`                 |
| `audio-id3v2.3.mp3` | plays, `maxTimeMs=293`                                                      | ready, `durationMs=6072`                       | same as above                                                                  |
| `audio-id3v2.4.mp3` | plays, `maxTimeMs=294`                                                      | ready, `durationMs=6072`                       | same as above                                                                  |
| `audio.flac`        | plays, `maxTimeMs=291`, `lengthMs=1000`                                     | `MediaException: Unrecognized file signature!` | `IllegalArgumentException: Unsupported conversion` (24-bit FLAC to 16-bit PCM) |
| `audio.m4a`         | plays, `maxTimeMs=291`, `lengthMs=1000`                                     | ready, `durationMs=1000`                       | `UnsupportedAudioFileException: File of unsupported format`                    |
| `audio.ogg`         | player reports `playing=true`; `time` stays 0 and `length` is -1 (see note) | `MediaException: Unrecognized file signature!` | container recognised (`type=OGG`, `durationUs=1000000`) but 0 bytes decoded    |

Note on the ogg fixture: it is structurally valid (5 pages, EOS flag on the last page,
final granule position 44100 = 1 s at 44.1 kHz). With `--verbose=2`, LibVLC logs
`using demux module "ogg"`, `using audio decoder module "vorbis"`, then opens the `pulse`
audio output on the physical sink; playback of the whole 1 s stream is buffered in 1 ms. The
`time == 0` reading is a measurement artifact of sampling a 1 s file whose demuxer reports no
length, not a decode failure. The same fixture produces 0 decoded bytes under the JavaSound
SPI, so it is a weak signal in both directions and is excluded from the ranking.

| Criterion                          | A: vlcj 4.12.1                                                                               | B: JavaFX Media 26                                                                                                                                | C: Java Sound SPI                                                                                       |
| ---------------------------------- | -------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------- |
| Containers decoded (of 6 fixtures) | 6 (5 with observable progress)                                                               | 4                                                                                                                                                 | 1                                                                                                       |
| Jar footprint                      | 399 KB + 93 KB natives + 1474 KB JNA + 2671 KB JNA-platform = **4.6 MB**                     | 731 KB base + 5338 KB graphics + 1541 KB media = **7.6 MB**                                                                                       | 33 + 19 + 79 + 129 KB = **260 KB**                                                                      |
| Native requirement                 | system LibVLC 3.0.x; on Debian/Ubuntu a package (`libvlc5` + `vlc-plugin-base`), no bundling | 22 `.so` files inside the `linux` classifier jars; loaded from the classpath, but JavaFX modules do not fit Compose Desktop's jlink image cleanly | none                                                                                                    |
| JDK requirement                    | 11+ (ran on toolchain 21)                                                                    | class file 68 → **JDK 24+**; fails on toolchain 21 with `UnsupportedClassVersionError`                                                            | 8+ (ran on toolchain 21)                                                                                |
| API completeness                   | full: play/pause/stop, `setTime` seek, volume, rate, event listeners, media parsing          | full: seek, volume, status properties, error callbacks                                                                                            | low: raw `AudioInputStream` + `SourceDataLine`; seek, volume and duration tracking are application work |
| Maintenance                        | 4.12.1 published 2025-12                                                                     | 26 current stable                                                                                                                                 | soundlibs last published 2016                                                                           |

## How the ranking was applied

The plan's locked order is: format coverage > packaging complexity > maintenance > API
completeness.

1. **Format coverage** eliminates C outright (1 of 6 fixtures) and leaves a clear gap between
   A (6 of 6 decoded) and B (4 of 6: no FLAC, no Ogg).
2. **Packaging complexity** would otherwise favour C, but C is already out. Between A and B:
   B forces the whole build onto JDK 24+ and drags JavaFX modules into a Compose Desktop
   jlink image; A needs one line in the deb metadata (`Depends: libvlc5, vlc-plugin-base`).
   The remaining risk for A is a machine without LibVLC, handled at runtime by a readable
   error plus the package dependency.
3. **Maintenance** is a wash between A and B and no longer decides anything.
4. **API completeness** is a wash as well; both expose seek and volume.

Result: **A**.

## Rejected candidates

- **vlcj 3.10.1** — same native story as 4.12.1, but frozen since 2015 and stuck on the flat
  API. Kept only as recorded evidence that the 4.x line was the right thing to re-test
  against LibVLC 3.
- **JavaFX Media** — rejected on coverage first (no FLAC, no Ogg) and packaging second
  (JDK 24+ runtime, JavaFX natives and modules inside a Compose Desktop image).
- **Java Sound SPI** — rejected on coverage: mp3 works end to end, FLAC fails at the
  24-bit-to-16-bit conversion, M4A is unsupported, and Ogg decodes nothing. Its small
  footprint and zero native dependencies do not compensate.

## Follow-ups implied by this choice

- `:desktop:packageDistributionForCurrentOS` must emit a deb whose metadata depends on
  `libvlc5` and `vlc-plugin-base`; T6 verifies the installed artifact, and the deb section
  records it.
- Startup must fail softly when LibVLC cannot be found: `NativeDiscovery` returns false and
  the UI needs a message instead of a stack trace.
- Bundling LibVLC for platforms without a package manager (Windows, macOS) is out of scope
  for D3 and belongs to a later packaging task.
- If LibVLC ever disappears from the target distro, JavaFX Media is the fallback worth
  revisiting, accepting loss of FLAC/Ogg.
