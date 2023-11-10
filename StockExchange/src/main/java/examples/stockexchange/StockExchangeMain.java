package examples.stockexchange;

import examples.codegen.stockexchange.IOU;
import examples.codegen.stockexchange.Stock;
import examples.stockexchange.parties.Bank;
import examples.stockexchange.parties.Buyer;
import examples.stockexchange.parties.Seller;
import examples.stockexchange.parties.StockExchange;
import com.daml.ledger.javaapi.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockExchangeMain {
  private static Logger logger = LoggerFactory.getLogger(StockExchangeMain.class);
  
  public static void main(String[] args) throws Exception {
    try (Bank bank = new Bank(5021);
         Buyer buyer = new Buyer(5031);
         StockExchange stockExchange = new StockExchange(5011);
         Seller seller = new Seller(5041)) {
      runExchange(bank, buyer, stockExchange, seller);
    }
  }

  private static void runExchange(
      Bank bank, Buyer buyer, StockExchange stockExchange, Seller seller) {
    String stockId = "Daml";

    logger.info("STEP 1: The Bank issues the IOU to the Buyer");
    IOU.ContractId iouCid = bank.issueIou(buyer.getPartyId(), 10L);

    logger.info("STEP 2a: The StockExchange issues the Stock with stockId `Daml` to the party Seller");
    Stock.ContractId stockContractId = stockExchange.issueStock(stockId, seller.getPartyId());

    logger.info("STEP 2b: The StockExchange emits a price quotation for the `Daml` stockId");
    stockExchange.emitPriceQuotation(stockId, 3L);

    logger.info("STEP 3: The Seller announces its intention to sell is stock by creating an Offer contract");
    seller.announceStockSaleOffer(stockExchange.getPartyId(), stockContractId);

    logger.info("STEP 4: The stakeholder parties are fetching their contracts for off-ledger " +
            "sharing to enable ANY buyer to accept the exchange");
    DisclosedContract priceQuotation = stockExchange.getPriceQuotationForDisclosure();
    DisclosedContract offer = seller.getOfferForDisclosure();
    DisclosedContract stock = seller.getStockForDisclosure();

    logger.info("STEP 5: The Buyer uses the provided disclosed contracts as attachments to the command submission " +
            "in which it accepts the offer and seals the exchange");
    buyer.buyStock(priceQuotation, offer, stock, iouCid);

    logger.info("Exchange executed successfully");
  }
}
