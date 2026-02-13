#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
LOCK_FILE="$ROOT_DIR/frontend/package-lock.json"

if [[ ! -f "$LOCK_FILE" ]]; then
  echo "[license-check] WARN: frontend/package-lock.json not found, skipping frontend license check."
  exit 0
fi

python3 - "$LOCK_FILE" <<'PY'
import json
import sys
from pathlib import Path

lock_path = Path(sys.argv[1])
with lock_path.open('r', encoding='utf-8') as f:
    data = json.load(f)

# Keep policy intentionally conservative: block strong/weak copyleft families by default.
# SPDX expressions may contain operators and exceptions, so do substring matching.
blocked_tokens = [
    'GPL',
    'AGPL',
    'LGPL',
    'CC-BY-SA',
    'EUPL',
]

issues = []
for pkg_name, pkg in (data.get('packages') or {}).items():
    lic = (pkg or {}).get('license')
    if not lic:
        # npm packages may omit license in lockfile metadata; tolerate unknowns here.
        continue

    license_text = str(lic).upper()
    if any(token in license_text for token in blocked_tokens):
        normalized_name = pkg_name or '<root>'
        issues.append((normalized_name, str(lic)))

if issues:
    print('[license-check] ERROR: Found disallowed licenses in frontend/package-lock.json:')
    for name, lic in issues:
        print(f'  - {name}: {lic}')
    sys.exit(1)

print('[license-check] OK: No disallowed frontend licenses detected.')
PY
