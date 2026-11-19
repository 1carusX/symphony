#!/usr/bin/env bash
# Builds dist/Symphony-Desktop-<version>-x86_64.AppImage: the Compose/jpackage
# app image (bundled JBR) plus the bundled libvlc runtime, wrapped into an
# AppDir and packed with appimagetool (downloaded on first use).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
BUILD_DIR="$ROOT/desktop/build"
DIST_DIR="$ROOT/dist"
APPIMAGETOOL="$BUILD_DIR/tools/appimagetool"
RUNTIME_DIR="$BUILD_DIR/vlc-runtime"
APP_DIR="$BUILD_DIR/appdir"

"$ROOT/desktop/packaging/bundle-vlc-runtime.sh" >/dev/null
(cd "$ROOT" && ANDROID_HOME="${ANDROID_HOME:-/home/icarus/Android/Sdk}" ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-/home/icarus/Android/Sdk}" ./gradlew :desktop:packageAppImage --console=plain -q)
APP_IMAGE_SRC=$(find "$BUILD_DIR/compose/binaries/main/app" -mindepth 1 -maxdepth 1 -type d | head -1)

if [ ! -x "$APPIMAGETOOL" ]; then
  mkdir -p "$(dirname "$APPIMAGETOOL")"
  curl -fL -o "$APPIMAGETOOL" https://github.com/AppImage/appimagetool/releases/download/continuous/appimagetool-x86_64.AppImage
  chmod +x "$APPIMAGETOOL"
fi

rm -rf "$APP_DIR"
mkdir -p "$APP_DIR"
cp -r "$APP_IMAGE_SRC/." "$APP_DIR/"
cp -r "$RUNTIME_DIR" "$APP_DIR/vlc-runtime"
cp -r "$ROOT/desktop/packaging/legal" "$APP_DIR/legal"

launcher=$(find "$APP_DIR/bin" -maxdepth 1 -type f | head -1)

cat > "$APP_DIR/AppRun" <<EOF
#!/usr/bin/env bash
HERE="\$(cd "\$(dirname "\$0")" && pwd)"
export LD_LIBRARY_PATH="\$HERE/vlc-runtime/lib\${LD_LIBRARY_PATH:+:\$LD_LIBRARY_PATH}"
export SYMPHONY_VLC_PATH="\$HERE/vlc-runtime/lib"
exec "\$HERE/bin/$(basename "$launcher")" "\$@"
EOF
chmod +x "$APP_DIR/AppRun"

cat > "$APP_DIR/symphony-desktop.desktop" <<EOF
[Desktop Entry]
Name=Symphony Desktop
Comment=Music player
Exec=desktop
Icon=symphony-desktop
Type=Application
Categories=Audio;Player;
Terminal=false
EOF

python3 - "$APP_DIR/symphony-desktop.png" <<'EOF'
import sys
from PIL import Image, ImageDraw
img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
d.ellipse((8, 8, 248, 248), fill=(103, 80, 164, 255))
d.polygon([(96, 64), (96, 176), (192, 120)], fill=(255, 255, 255, 255))
d.ellipse((76, 168, 116, 208), fill=(255, 255, 255, 255))
img.save(sys.argv[1])
EOF

mkdir -p "$DIST_DIR"
ARCH=x86_64 "$APPIMAGETOOL" --appimage-extract-and-run "$APP_DIR" "$DIST_DIR/Symphony-Desktop-0.1.0-x86_64.AppImage" 2>&1 | tail -3
ls -l "$DIST_DIR/"
