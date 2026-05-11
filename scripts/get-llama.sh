#!/bin/sh
set -e

DEST="$HOME/ai"
LLAMA_VER="b9101"
BASE_URL="https://github.com/ggml-org/llama.cpp/releases/download/${LLAMA_VER}"

detect_arch() {
  case $(uname -m) in
    aarch64|arm64) echo "arm64" ;;
    armv7l)        echo "armhf" ;;
    x86_64)        echo "x64"   ;;
    *)             echo "x64"   ;;
  esac
}

ARCH=$(detect_arch)
FILE="llama-${LLAMA_VER}-bin-ubuntu-${ARCH}.zip"
URL="${BASE_URL}/${FILE}"

echo "=== Download llama.cpp ${LLAMA_VER} untuk ${ARCH} ==="
echo "URL: $URL"
mkdir -p $DEST

if command -v wget > /dev/null 2>&1; then
  wget -q --show-progress -O /tmp/llama.zip "$URL"
else
  curl -L --progress-bar -o /tmp/llama.zip "$URL"
fi

echo "Extract..."
unzip -jo /tmp/llama.zip "*/llama-cli" -d $DEST/ 2>/dev/null || \
unzip -jo /tmp/llama.zip "*/main"      -d $DEST/ 2>/dev/null && \
  mv $DEST/main $DEST/llama-cli 2>/dev/null || true

chmod +x $DEST/llama-cli
rm /tmp/llama.zip
echo "OK: $(file $DEST/llama-cli)"
