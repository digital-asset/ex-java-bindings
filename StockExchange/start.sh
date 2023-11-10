#!/usr/bin/env bash
set -eo pipefail

CANTON_PATH=$1

if [[ -z "$CANTON_PATH" ]]; then
  echo "Pass the path to the Canton install dir"
  exit 1
fi

echo "Compiling daml"
daml build

echo "Generating java code"
daml codegen java

echo "Compiling code"
mvn compile

function cleanup(){
    if [[ $cantonPID ]]; then
        # kill the sandbox which is running in the background
        kill $cantonPID
    fi
}

trap cleanup ERR EXIT

echo "Starting Canton"
"$CANTON_PATH"/bin/canton daemon -c stock-exchange.conf --bootstrap stock_exchange_script.canton

cantonPID=$!

echo "Running StockExchange"
mvn exec:java -Dexec.mainClass=examples.stockexchange.StockExchangeMain
