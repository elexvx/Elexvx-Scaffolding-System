#!/usr/bin/env bash
set -euo pipefail

file="THIRD_PARTY_NOTICES.md"
if [[ ! -f "$file" ]]; then
  echo "THIRD_PARTY_NOTICES.md not found" >&2
  exit 1
fi

if rg -n 'AGPL|GPL|LGPL|MPL|CDDL|FSL|SSPL' "$file"; then
  echo "Found disallowed license identifiers in THIRD_PARTY_NOTICES.md" >&2
  exit 1
fi

awk -F'|' '
/^[|]/ {
  lic=$4
  gsub(/^ +| +$/, "", lic)
  if (lic=="许可证" || lic=="---" || lic=="") next
  if (lic!="MIT" && lic!="Apache-2.0" && lic!="OFL-1.1") {
    print "Non-whitelisted license:", lic
    bad=1
  }
}
END { exit bad }
' "$file"

echo "License gate passed (MIT/Apache-2.0/OFL-1.1)"
