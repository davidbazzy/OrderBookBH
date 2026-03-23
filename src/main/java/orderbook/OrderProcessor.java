package orderbook;

import engine.MatchingEngine;
import logger.OrderBookLogger;
import model.Order;
import model.Side;
import model.TradeConfirmation;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OrderProcessor {

    private final BookSide buyBook;
    private final BookSide sellBook;
    private final MatchingEngine matchingEngine;
    private final Set<Integer> processedOrderIds = new HashSet<>();

    public OrderProcessor() {
        this.buyBook = new BookSide(
                (incomingPrice, bestBuyPrice) -> incomingPrice <= bestBuyPrice,
                (incomingPrice, currentPrice) -> incomingPrice > currentPrice);
        this.sellBook = new BookSide(
                (incomingPrice, bestSellPrice) -> incomingPrice >= bestSellPrice,
                (incomingPrice, currentPrice) -> incomingPrice < currentPrice);
        matchingEngine = new MatchingEngine();
    }

    /**
     * Process each order by validating uniqueness, conduct matching and place into its corresponding book if there's
     * any quantity available post-matching. Print trade confirmations and book summary post matching
     *
     * @param incomingOrder
     */
    public void processOrder(Order incomingOrder) {
        int orderId = incomingOrder.getOrderId();
        if (processedOrderIds.contains(orderId)) {
            // log rejection due to duplicate order id
            System.err.printf("Incoming order has a DUPLICATE order id: %d and therefore will be discarded%n", orderId);
            return;
        } else {
            processedOrderIds.add(orderId);
        }

        final Collection<TradeConfirmation> tradeConfirmations;
        if (incomingOrder.getSide() == Side.BUY) {
            tradeConfirmations = matchingEngine.matchIncomingOrder(incomingOrder, sellBook);
            buyBook.addToBook(incomingOrder);
        } else {
            tradeConfirmations = matchingEngine.matchIncomingOrder(incomingOrder, buyBook);
            sellBook.addToBook(incomingOrder);
        }

        OrderBookLogger.printTradeConfirmations(tradeConfirmations);
        OrderBookLogger.printBookSummary(buyBook.getOrders(), sellBook.getOrders());
    }

    public List<Order> getBuyOrders() {
        return buyBook.getOrders();
    }

    public List<Order> getSellOrders() {
        return sellBook.getOrders();
    }
}
