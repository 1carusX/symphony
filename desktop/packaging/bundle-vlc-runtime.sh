#!/usr/bin/env bash
# Extracts a self-contained, audio-focused libvlc runtime from the installed
# VLC packages into desktop/build/vlc-runtime/. Only local-file playback
# plugins are bundled so the transitive shared-library closure stays small
# (Debian's full libavcodec build alone drags in ~60 MB of video encoders).
# libvlc is LGPL-2.1+; see desktop/packaging/legal/ for the compliance files
# shipped alongside it.
set -euo pipefail

VLC_LIB_DIR="/usr/lib/x86_64-linux-gnu"
VLC_PLUGIN_DIR="${VLC_LIB_DIR}/vlc/plugins"
OUT_DIR="$(cd "$(dirname "$0")/../.." && pwd)/desktop/build/vlc-runtime"

PLUGINS=(
  access/libfilesystem_plugin.so
  audio_filter/libsamplerate_plugin.so
  audio_filter/libscaletempo_plugin.so
  audio_mixer/libfloat_mixer_plugin.so
  audio_mixer/libinteger_mixer_plugin.so
  audio_output/libpulse_plugin.so
  audio_output/libalsa_plugin.so
  audio_output/libadummy_plugin.so
  codec/libadpcm_plugin.so
  codec/libaes3_plugin.so
  codec/libaraw_plugin.so
  codec/libddummy_plugin.so
  codec/libedummy_plugin.so
  codec/libfaad_plugin.so
  codec/libflac_plugin.so
  codec/libg711_plugin.so
  codec/liblpcm_plugin.so
  codec/libmpg123_plugin.so
  codec/liboggspots_plugin.so
  codec/libopus_plugin.so
  codec/libspdif_plugin.so
  codec/libspeex_plugin.so
  codec/libuleaddvaudio_plugin.so
  codec/libvorbis_plugin.so
  demux/libes_plugin.so
  demux/libflacsys_plugin.so
  demux/libmp4_plugin.so
  demux/libogg_plugin.so
  demux/libwav_plugin.so
  logger/libconsole_logger_plugin.so
  logger/libfile_logger_plugin.so
  packetizer/libpacketizer_a52_plugin.so
  packetizer/libpacketizer_dts_plugin.so
  packetizer/libpacketizer_flac_plugin.so
  packetizer/libpacketizer_mlp_plugin.so
  packetizer/libpacketizer_mpeg4audio_plugin.so
  packetizer/libpacketizer_mpegaudio_plugin.so
  stream_extractor/libarchive_plugin.so
  stream_filter/libadf_plugin.so
  stream_filter/libprefetch_plugin.so
  stream_filter/libskiptags_plugin.so
)

EXCLUDE_PATTERN='^(linux-vdso\.so|ld-linux.*|libc\.so\.|libm\.so\.|libdl\.so\.|libpthread\.so\.|librt\.so\.|libresolv\.so\.|libutil\.so\.|libnsl\.so\.)'

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR/lib"

cp -L "${VLC_LIB_DIR}/libvlc.so.5" "$OUT_DIR/lib/"
cp -L "${VLC_LIB_DIR}/libvlccore.so.9" "$OUT_DIR/lib/"
cp -L "${VLC_LIB_DIR}/vlc/libvlc_pulse.so.0" "$OUT_DIR/lib/" 2>/dev/null || true
ln -sf libvlc.so.5 "$OUT_DIR/lib/libvlc.so"
ln -sf libvlccore.so.9 "$OUT_DIR/lib/libvlccore.so"

for plugin in "${PLUGINS[@]}"; do
  dest="$OUT_DIR/vlc/plugins/$(dirname "$plugin")"
  mkdir -p "$dest"
  cp -L "${VLC_PLUGIN_DIR}/${plugin}" "$dest/"
done

if command -v vlc-cache-gen >/dev/null 2>&1; then
  vlc-cache-gen "$OUT_DIR/vlc/plugins" >/dev/null 2>&1 || true
fi

declare -A SEEN
collect() {
  local binary="$1" dep path
  while read -r dep path; do
    [[ "$path" =~ ^/ ]] || continue
    [[ "$dep" =~ $EXCLUDE_PATTERN ]] && continue
    [[ -n "${SEEN[$dep]:-}" ]] && continue
    SEEN[$dep]=1
    cp -L "$path" "$OUT_DIR/lib/"
    collect "$path"
  done < <(ldd "$binary" 2>/dev/null | awk '/=> \// {print $1, $3}')
}

while IFS= read -r -d '' so; do
  collect "$so"
done < <(find "$OUT_DIR" -name '*.so*' -print0)

{
  echo "bundled-vlc-version: $(dpkg-query -W -f='${Version}' libvlc5 2>/dev/null || echo unknown)"
  echo "bundled-at: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
  echo "libraries: $(ls "$OUT_DIR/lib" | wc -l)"
  echo "plugins: $(find "$OUT_DIR/vlc/plugins" -name '*.so' | wc -l)"
} > "$OUT_DIR/MANIFEST.txt"

du -sh "$OUT_DIR" "$OUT_DIR/lib" "$OUT_DIR/vlc"
cat "$OUT_DIR/MANIFEST.txt"
