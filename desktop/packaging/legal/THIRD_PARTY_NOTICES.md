# Third-Party Notices — Symphony Desktop

This distribution bundles the following third-party components. Each remains
under its own license; the texts required to be shipped are included next to
this file.

## libvlc and VLC plugins (vlc-runtime/)

- **libvlc, libvlccore and VLC plugins** — LGPL-2.1-or-later.
  Copyright (C) VideoLAN and VLC authors. https://www.videolan.org/vlc/
  Complete corresponding source code for the bundled version (Debian
  3.0.24-0+deb13u1) is available from https://www.videolan.org/vlc/download-sources.html
  and https://sources.debian.org/src/vlc/3.0.24-0+deb13u1/.
  The library is loaded dynamically (via JNA); it can be replaced by any
  compatible libvlc build by swapping the files under vlc-runtime/lib and
  vlc-runtime/vlc/plugins. The license text ships in legal/LGPL-2.1.txt.
  The bundle intentionally excludes GPL-licensed VLC plugins (libx264,
  libx265 encoders) — no GPL code is distributed.

## Audio codec libraries linked by the bundled plugins

- **FAAD2** (AAC decoding) — GPL-2.0 license _with the "no charge for
  distribution" clarification_, see https://www.audiocoding.com; bundled as
  Debian libfaad2. Note: FAAD2 is GPL-2.0. Its source is at
  https://sources.debian.org/src/faad2/. (Included dynamically linked.)
- **mpg123** (MP3 decoding) — LGPL-2.1. https://www.mpg123.de
- **FLAC** — BSD-3-Clause. https://xiph.org/flac
- **libvorbis / libogg** — BSD-3-Clause. https://xiph.org
- **libopus** — BSD-3-Clause. https://opus-codec.org
- **libspeex / libspeexdsp** — BSD-3-Clause. https://speex.org
- **libsamplerate** — BSD-2-Clause. https://libsndfile.github.io/libsamplerate
- **libsndfile** — LGPL-2.1-or-later. https://libsndfile.github.io/libsndfile
- **PulseAudio client libraries** — LGPL-2.1-or-later. https://www.freedesktop.org/wiki/Software/PulseAudio
- **ALSA library** — LGPL-2.1-or-later. https://www.alsa-project.org
- **libarchive** — BSD-2-Clause. https://www.libarchive.org

## Java/Kotlin application stack

- **JetBrains Runtime (OpenJDK)** — GPL-2.0 with Classpath Exception.
  https://github.com/JetBrains/JetBrainsRuntime
- **Kotlin stdlib, JetBrains Compose Multiplatform** — Apache-2.0.
- **vlcj** — GPL-3.0-or-later. https://capricasoftware.co.uk
  Symphony's desktop module is distributed under GPL-3.0 as a combined work
  with vlcj; Symphony's own source is GPL-3.0 (see repository LICENSE).
- **jaudiotagger** — LGPL-2.1. https://www.jthink.net/jaudiotagger

Source code for Symphony: https://github.com/zyrouge/symphony
