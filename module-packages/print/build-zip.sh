#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
TMP_DIR="$ROOT_DIR/.build-tmp"
DIST_DIR="$ROOT_DIR/dist"
ZIP_FILE="$DIST_DIR/module-print.zip"

rm -rf "$TMP_DIR"
mkdir -p "$TMP_DIR" "$DIST_DIR"

pushd "$ROOT_DIR/frontend-app" >/dev/null
npm ci --no-audit --no-fund
npm run build
popd >/dev/null

cp "$ROOT_DIR/module.json" "$TMP_DIR/module.json"
cp -R "$ROOT_DIR/frontend" "$TMP_DIR/frontend"
cp -R "$ROOT_DIR/backend" "$TMP_DIR/backend"
cp -R "$ROOT_DIR/modules" "$TMP_DIR/modules"

pushd "$TMP_DIR" >/dev/null
zip -rq "$ZIP_FILE" module.json frontend backend modules
popd >/dev/null

echo "built: $ZIP_FILE"
