#!/bin/bash
#
# Which beCPG build is actually running on an instance.
#
# "Is my fix deployed?" used to mean reading a Cloud Build log, guessing at the
# image tag, and hoping the rollout had finished. The answer was already served
# by the repository itself: /becpg/check exposes `becpgSchema`, built from the
# `becpg.schema` property as <git-sha>-<version>-<build-epoch-ms>.
#
# The catch, and the reason nobody used it: the webscript only fills
# `systemInfo` when the request carries the User-Agent the monitors send
# (MonitorWebScript.MONITORS_USER_AGENT). Without it the response is
# `{"status": "200"}` and looks like a bare liveness probe.
#
#   ./becpg-version.sh dev.becpg.fr
#   ./becpg-version.sh dev.becpg.fr qa.becpg.fr localhost:8080
#
# Compare against a commit to settle "is it in?":
#
#   ./becpg-version.sh dev.becpg.fr | grep sha        # -> 32061d6
#   git merge-base --is-ancestor 32061d6 HEAD && echo "dev is behind HEAD"
#
set -euo pipefail

USER_AGENT="beCPG Monitors"

if [[ $# -eq 0 ]]; then
  echo "usage: $(basename "$0") <host> [host ...]" >&2
  echo "  host: dev.becpg.fr, qa.becpg.fr, localhost:8080, https://…" >&2
  exit 2
fi

# A bare host gets https, except localhost which is virtually always plain http
# in a docker-compose stack.
url_for() {
  local host="$1"
  case "${host}" in
    http://*|https://*) printf '%s' "${host}" ;;
    localhost*|127.0.0.1*) printf 'http://%s' "${host}" ;;
    *) printf 'https://%s' "${host}" ;;
  esac
}

status=0

for host in "$@"; do
  base="$(url_for "${host}")"
  body="$(curl -sS --max-time 30 -H "User-Agent: ${USER_AGENT}" \
    "${base}/alfresco/s/becpg/check" 2>/dev/null || true)"

  if [[ -z "${body}" ]]; then
    printf '%-24s unreachable\n' "${host}"
    status=1
    continue
  fi

  # The template emits raw tabs and newlines inside the JSON, so parse it rather
  # than grepping: python is present anywhere this script is useful.
  printf '%s' "${body}" | HOST="${host}" python3 -c '
import json, os, sys, datetime

host = os.environ["HOST"]
try:
    payload = json.load(sys.stdin)
except ValueError:
    print("%-24s unparseable response" % host)
    sys.exit(1)
info = payload.get("systemInfo") or {}
# solr_status sits next to systemInfo, not inside it.
solr = payload.get("solr_status")

schema = info.get("becpgSchema")
if not schema:
    # Reached the repository but it did not honour the User-Agent: either an
    # older beCPG, or something in front (a WAF, an ingress) stripped it.
    print("%-24s no systemInfo — User-Agent not honoured" % host)
    sys.exit(1)

# <sha>-<version>-<epoch-ms>, but stay readable if the shape ever changes.
parts = schema.split("-")
built = ""
if len(parts) >= 3 and parts[-1].isdigit():
    built = datetime.datetime.fromtimestamp(int(parts[-1]) / 1000).strftime("%Y-%m-%d %H:%M")
    sha, version = parts[0], "-".join(parts[1:-1])
else:
    sha, version = parts[0], "-".join(parts[1:])

print("%-24s %-12s sha %-10s built %s%s" % (
    host, version, sha, built,
    "" if solr == "UP" else "  [solr %s]" % (solr or "?"),
))
' || status=1
done

exit "${status}"
