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
    fi
}

trap cleanup ERR EXIT

echo "Compiling daml"
daml build
packageId=$(daml damlc inspect-dar --json .daml/dist/ex-java-bindings-0.0.2.dar | jq '.main_package_id' -r)


echo "Generating java code"
daml codegen java

echo "Compiling code"
mvn compile

# Could also run this manually in another terminal without the redirects
echo "Starting sandbox"
daml start --start-navigator false --sandbox-port 7600 > sandbox.log 2>&1 & PID=$!


while [[ "$(getSandboxPid)" -eq '' ]]
do
    sleep 1
done

# Run java program
mvn exec:java -Dexec.mainClass=$1 -Dpackage.id=$packageId -Dexec.args="localhost 7600"
