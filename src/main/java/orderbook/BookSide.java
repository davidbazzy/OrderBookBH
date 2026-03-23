package orderbook;

import model.Order;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiPredicate;

public class BookSide {
    private final LinkedList<PriceLevel> priceLevels;

    // Rule used to evaluate match between incoming order price and best price in the book
    private final BiPredicate<Integer, Integer> matchRule;

    // Rule used to evaluate insertion for an incoming order into its own book
    private final BiPredicate<Integer, Integer> insertionRule;

    public BookSide(BiPredicate<Integer, Integer> matchRule, BiPredicate<Integer, Integer> insertionRule) {
        this.priceLevels = new LinkedList<>();
        this.matchRule = matchRule;
        this.insertionRule = insertionRule;
    }

    /**
     * Adds order to book via the {@link PriceLevel} object if the order has any quantity available.
     *
     * The order is placed using a defined insertion rule that's defined when instantiating the BookSide object.
     * Eg: For the SELL book, the lower the price, the closer the object to the start of the list
     *
     * @param incomingOrder
     */
    public void addToBook(Order incomingOrder) {
        if (incomingOrder.getAvailableQuantity() > 0) {
            int incomingOrderPrice = incomingOrder.getPrice();

            ListIterator<PriceLevel> iterator = priceLevels.listIterator();

            while (iterator.hasNext()) {
                PriceLevel currentPriceLevel = iterator.next();

                if (currentPriceLevel.getPrice() == incomingOrderPrice) {
                    currentPriceLevel.add(incomingOrder);
                    return;
                } else if (insertionRule.test(incomingOrderPrice, currentPriceLevel.getPrice())) {
                    iterator.previous();
                    iterator.add(new PriceLevel(incomingOrder));
                    return;
                }
            }
            iterator.add(new PriceLevel(incomingOrder));
        }
    }

    public void removeHead() {
        if(!priceLevels.isEmpty()) {
            priceLevels.removeFirst();
        }
    }

    public PriceLevel bestMatchingPriceLevel(int incomingPrice) {
        PriceLevel firstPriceLevel = priceLevels.peekFirst();

        if (firstPriceLevel != null && matchRule.test(incomingPrice, firstPriceLevel.getPrice())) {
            return firstPriceLevel;
        }

        return null;
    }

    public List<Order> getOrders() {
        return  priceLevels.stream().flatMap(priceLevel -> priceLevel.getOrders().stream()).toList();
    }
}