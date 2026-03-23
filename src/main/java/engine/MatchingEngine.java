package engine;

import model.Order;
import model.PostFillAction;
import model.TradeConfirmation;
import orderbook.BookSide;
import orderbook.PriceLevel;

import java.util.*;

public final class MatchingEngine {

    /**
     * Attempts to match an incoming order against the opposing book side (a list of PriceLevel objects)
     *
     * This method iterates through each price level in the book starting from the best available price.
     * If a price level is empty (due to orders being filled at that level), it is removed from the book.
     *
     * @param incomingOrder
     * @param book
     * @return A Collection of {@link TradeConfirmation} objects
     */
    public Collection<TradeConfirmation> matchIncomingOrder(Order incomingOrder, BookSide book) {
        // initial check to see whether a match is possible - exit early if not
        PriceLevel matchedPriceLevel = book.bestMatchingPriceLevel(incomingOrder.getPrice());
        if (matchedPriceLevel == null) return Collections.emptyList();

        Map<Integer, TradeConfirmation> bookfills = new LinkedHashMap<>();

        // iterate price Level queue & exit if incoming is fully filled or queue is empty
        while (incomingOrder.getMatchableQuantity() > 0) {
            matchOrderAtPriceLevel(incomingOrder, matchedPriceLevel, bookfills);
            if (matchedPriceLevel.isEmpty()) book.removeHead();

            // Break if there is no upcoming price level
            matchedPriceLevel = book.bestMatchingPriceLevel(incomingOrder.getPrice());
            if (matchedPriceLevel == null) break;
        }

        // Refill incomingOrder available quantity before exiting matching (if applicable)
        incomingOrder.refillAvailableQuantity();
        return bookfills.values();
    }

    /**
     * Matches an order against all possible orders at a given {@link PriceLevel}.
     *
     * Orders are processed in FIFO order. After a fill has occurred, postFill() is called on the resting Order to
     * determine what action to carry out with the resting order (eg: Remove if empty, keep if not fully filled, rotate
     * if peak refill is possible, which also calls refillAvailableQuantity to refill the order)
     *
     * @param incomingOrder
     * @param priceLevel
     * @param bookfills
     */
    private void matchOrderAtPriceLevel(Order incomingOrder, PriceLevel priceLevel, Map<Integer,TradeConfirmation> bookfills) {
        // iterate price Level & exit if incoming fully filled or price level is exhausted
        while (incomingOrder.getMatchableQuantity() > 0 && !priceLevel.isEmpty()) {
            Order restingOrder = priceLevel.peekFirst();
            int quantityToFill = Math.min(incomingOrder.getMatchableQuantity(), restingOrder.getAvailableQuantity());
            incomingOrder.fill(quantityToFill);
            restingOrder.fill(quantityToFill);

            // Call postFill() for resting order to cater for any post fill actions (eg: rotate for iceberg)
            PostFillAction restingOrderPostFillAction = restingOrder.postFill();

            switch(restingOrderPostFillAction) {
                case ROTATE -> {
                    restingOrder.refillAvailableQuantity();
                    priceLevel.rotateHead();
                }
                case REMOVE -> priceLevel.removeHead();
                case KEEP -> {} // No-op
            }

            // Add fill to trade confirmation map
            addTradeConfirmation(bookfills, incomingOrder, restingOrder.getOrderId(), quantityToFill);
        }
    }

    /**
     * Create {@link TradeConfirmation} objects that are used for aggregating fills for a given incoming and resting
     * order fill.
     *
     * @param bookfills
     * @param incomingOrder
     * @param restingOrderId
     * @param quantityToFill
     */
    private void addTradeConfirmation(Map<Integer,TradeConfirmation> bookfills, Order incomingOrder, int restingOrderId, int quantityToFill) {
        TradeConfirmation existing = bookfills.get(restingOrderId);

        if (existing == null) {
            bookfills.put(restingOrderId, new TradeConfirmation(restingOrderId, incomingOrder, quantityToFill));
        } else {
            existing.updateFilledQuantity(quantityToFill);
        }
    }
}
