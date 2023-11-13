#!/usr/bin/env bash
set -o pipefail

function build_example() {
  echo "Compiling daml"
  daml build

  echo "Generating java code"
  daml codegen java

  echo "Compiling code"
  mvn compile
}

function start_canton() {
  CANTON_PATH=$1
  if [[ -z "$CANTON_PATH" ]]; then
    echo "Pass the path to the Canton install dir"
  else
    echo "Starting Canton"
    "$CANTON_PATH"/bin/canton daemon -c canton_ledger.conf --bootstrap stock_exchange_bootstrap_script.canton
  fi
}

function run_stock_exchange() {
  echo "Running StockExchange"
  mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.Bank
  mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.StockExchange
  mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.Seller
  mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.Buyer
  echo "Finished StockExchange example"
}
