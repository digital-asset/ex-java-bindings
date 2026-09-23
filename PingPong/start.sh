#!/usr/bin/env bash
set -euo pipefail

function pid_from_listen_line(){
    perl -ne 'print "$1\n" if /pid=([0-9]+)/'
}

function getSandboxPid(){
    port=${1:-7575}
    if command -v ss >/dev/null 2>&1; then
        ss -lptn "sport = :$port" | pid_from_listen_line
    else
        lsof -nP -iTCP:"$port" -sTCP:LISTEN -t 2>/dev/null || true
    fi
}
function cleanup(){
    echo "Cleaning up"
    sandboxPID=$(getSandboxPid)
    if [[ $sandboxPID ]]; then
        # kill the sandbox which is running in the background
        kill $sandboxPID
        rm ports.json
        echo "Done"
    fi
}

if [[ "${BASH_SOURCE[0]}" != "$0" ]]; then
    return 0
fi

trap cleanup ERR EXIT

echo "Compiling daml"
dpm build --all

pushd main
packageId=$(dpm damlc inspect-dar --json .daml/dist/ex-java-bindings-0.0.2.dar | jq '.main_package_id' -r)

echo "Generating java code"
dpm codegen-java
popd

echo "Compiling code"
mvn compile

# Could also run this manually in another terminal without the redirects
echo "Starting sandbox"
dpm sandbox \
  --ledger-api-port 7600 \
  --json-api-port 7575 \
  --canton-port-file ports.json \
  --dar main/.daml/dist/ex-java-bindings-0.0.2.dar \
    > sandbox.log 2>&1 & PID=$!

echo "Waiting for sandbox to write the port file"
until [ -e ports.json ]; do sleep 1; done

echo "Running the script"
dpm script \
  --dar script/.daml/dist/ex-java-bindings-script-0.0.2.dar \
  --ledger-host localhost \
  --ledger-port 7600 \
  --script-name PingPongTest:setup


while [[ "$(getSandboxPid)" -eq '' ]]
do
    sleep 1
done

echo "Running the java program"
mvn exec:java -Dexec.mainClass=$1 -Dpackage.id=$packageId -Dexec.args="localhost 7600"
