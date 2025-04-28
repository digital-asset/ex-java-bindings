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

function upload_package() {
  version=${1:-"1.0.0"}
  for port in 5011 5021 5031 5041 ;
  do
    daml ledger upload-dar --host localhost --port "${port}" .daml/dist/ex-java-bindings-stock-exchange-"${version}".dar
  done

}

function run_stock_exchange() {
  echo "Running StockExchange"
  stockExchangePartiesFile="temp_stock_exchange_example/stock_exchange_parties.txt"
  if [ -e "$stockExchangePartiesFile" ]; then
    buyerPartyId=$(sed -n "3p" $stockExchangePartiesFile)
    sellerPartyId=$(sed -n "4p" $stockExchangePartiesFile)
    stockExchangePartyId=$(sed -n "1p" $stockExchangePartiesFile)
    mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.Bank -Dexec.args="5021 Bank ""$buyerPartyId"" 10"
    mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.StockExchange -Dexec.args="5011 StockExchange ""$sellerPartyId"" Daml 3"
    mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.Seller -Dexec.args="5041 Seller ""$stockExchangePartyId"""
    mvn exec:java -Dexec.mainClass=examples.stockexchange.parties.Buyer -Dexec.args="5031 Buyer"
    echo "Finished StockExchange example"
  else
    echo "'$stockExchangePartiesFile' does not exist. Check that the current user has write rights in the current dir and run start_canton <path_to_canton_dir> before running this function"
  fi
}
