#!/usr/bin/env bash
set -eo pipefail
root=$(cd "$(dirname "$0")/../.." && pwd)

set +e
printf '%s\n' 'users:(("java",pid=4321,fd=5))' | grep -P -o '(?<=pid=)([0-9]+)' >/tmp/exj-grep.out 2>/tmp/exj-grep.err
red=$?
set -e
if [ "$red" -eq 0 ]; then
  echo "grep -P unexpectedly succeeded" >&2
  exit 1
fi
grep -q "invalid option" /tmp/exj-grep.err

set +e
ss -lptn 'sport = :7575' >/tmp/exj-ss.out 2>/tmp/exj-ss.err
ssred=$?
set -e
if [ "$ssred" -eq 0 ]; then
  echo "ss unexpectedly succeeded" >&2
  exit 1
fi
grep -q "command not found" /tmp/exj-ss.err

# shellcheck disable=SC1091
source "$root/PingPong/start.sh"
got=$(printf '%s\n' 'users:(("java",pid=4321,fd=5))' | pid_from_listen_line)
[ "$got" = "4321" ]

port=18765
python3 -m http.server "$port" >/tmp/exj-http.out 2>&1 &
http=$!
cleanup_http() { kill "$http" >/dev/null 2>&1 || true; }
trap cleanup_http EXIT
for _ in 1 2 3 4 5 6 7 8 9 10; do
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN -t >/dev/null 2>&1; then
    break
  fi
  sleep 0.1
done
pids=$(getSandboxPid "$port")
printf '%s\n' "$pids" | grep -q "^${http}$"
echo "ex-java-bindings darwin ok (grep -P exit $red, ss exit $ssred, pid $http)"
