#!/usr/bin/env bash
set -euo pipefail

function getSandboxPid(){
    ss -lptn 'sport = :7575' | grep -P -o '(?<=pid=)([0-9]+)'
}
function cleanup(){
    sandboxPID=$(ss -lptn 'sport = :7575' | grep -P -o '(?<=pid=)([0-9]+)')
    if [[ $sandboxPID ]]; then
        # kill the sandbox which is running in the background
        kill $sandboxPID
        rm ports.json
    fi
}

trap cleanup ERR EXIT

echo "Compiling daml"
daml build --all

pushd main
packageId=$(daml damlc inspect-dar --json .daml/dist/ex-java-bindings-0.0.2.dar | jq '.main_package_id' -r)

echo "Generating java code"
daml codegen java
popd

echo "Compiling code"
mvn compile

# Could also run this manually in another terminal without the redirects
echo "Starting sandbox"
daml sandbox \
  --port 7600 \
  --json-api-port 7575 \
  --canton-port-file ports.json \
  --dar main/.daml/dist/ex-java-bindings-0.0.2.dar \
    > sandbox.log 2>&1 & PID=$!

echo "Waiting for sandbox to write the port file"
until [ -e ports.json ]; do sleep 1; done

echo "Running the script"
daml script \
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
