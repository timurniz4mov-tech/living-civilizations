#!/usr/bin/env bash
set -u
BASE="https://freeskineditor.com/skin-bases"
OUT="src/main/resources/assets/livingcivilizations/textures/entity"
mkdir -p "$OUT"

# Original PixelPelt skins. The site states that its original artwork may be
# used, edited, shared and sold without requiring credit.
download_skin() {
  local role="$1"
  local file="$2"
  local target="$OUT/${role}.png"
  local tmp="${target}.download"
  echo "Downloading ${role} skin: ${file}"
  if curl --fail --location --silent --show-error --retry 3 --connect-timeout 15 \
      --user-agent "Mozilla/5.0 (compatible; LivingCivilizationsModBuild/1.0)" \
      --max-time 60 "${BASE}/${file}" -o "$tmp"; then
    if python3 - "$tmp" <<'PY'
import struct, sys
p = sys.argv[1]
with open(p, 'rb') as f:
    data = f.read(24)
if len(data) < 24 or data[:8] != b'\x89PNG\r\n\x1a\n':
    raise SystemExit('Downloaded file is not PNG')
w, h = struct.unpack('>II', data[16:24])
if (w, h) != (64, 64):
    raise SystemExit(f'Expected 64x64 skin, got {w}x{h}')
PY
    then
      mv "$tmp" "$target"
      echo "  OK: ${target}"
    else
      rm -f "$tmp"
      echo "  WARNING: wrong image format; keeping bundled fallback for ${role}" >&2
    fi
  else
    rm -f "$tmp"
    echo "  WARNING: source unavailable; keeping bundled fallback for ${role}" >&2
  fi
}

download_skin traveler          cool-viking-classic.png
download_skin caravan_merchant  boy-pirate-captain-classic.png
download_skin village_merchant  boy-farmer-classic.png
download_skin worker             boy-lumberjack-classic.png
download_skin guard              boy-knight-classic.png
download_skin warrior            cool-flame-knight-classic.png
download_skin lord               girl-royal-gown-classic.png
download_skin king               cool-shadow-king-classic.png
download_skin bandit             cool-masked-rogue-classic.png
