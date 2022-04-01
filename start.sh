#!/usr/bin/env bash
set -euo pipefail

daml build
packageId=$(daml damlc inspect-dar --json .daml/dist/ex-java-bindings-0.0.2.dar | jq '.main_package_id' -r)

mvn compile

# Could also run this manually in another terminal without the redirects
daml start --start-navigator false --sandbox-port 7600 < /dev/null >/dev/null 2>&1

mvn exec:java -Dexec.mainClass=examples.pingpong.reactive.PingPongReactiveMain -Dpackage.id=$packageId -Dexec.args="localhost 7600"
