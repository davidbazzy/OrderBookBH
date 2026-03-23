package engine;

import model.*;
import orderbook.BookSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MatchingEngineTest {

    private BookSide buyBook;
    private final MatchingEngine matchingEngine = new MatchingEngine();

    @BeforeEach
    void setUp() {
        buyBook = new BookSide(
                (incomingPrice, bestBuyPrice) -> incomingPrice <= bestBuyPrice,
                (incomingPrice, currentPrice) -> incomingPrice > currentPrice);
    }

    @Test
    public void testIncomingOrderEmptyBook() {
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 10);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        assertEquals(0, tradeConfirmations.size());
        assertTrue(buyBook.getOrders().isEmpty());
        assertEquals(10, incomingLimitOrder.getMatchableQuantity());
    }

    @Test
    public void testIncomingOrderNoFillMultipleRestingOrders() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        buyBook.addToBook(new LimitOrder(3,2, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,4, Side.SELL, 150);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        assertEquals(0, tradeConfirmations.size());
        assertEquals(150, incomingLimitOrder.getMatchableQuantity());

        // New incoming iceberg order, same price as previous incoming order. Should result in no match
        Order incomingIcebergOrder = new IcebergOrder(1,4, Side.SELL, 200, 40);
        tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        assertEquals(0, tradeConfirmations.size());
        assertEquals(200, incomingIcebergOrder.getMatchableQuantity());
    }

    @Test
    public void testIncomingOrderFullFillSingleRestingOrder() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 10);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingLimitOrder, 10);

        assertEquals(1, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertEquals(10, tradeConfirmations.iterator().next().getFilledQuantity());
        assertEquals(0, incomingLimitOrder.getMatchableQuantity());
    }

    @Test
    public void testIcebergIncomingOrderFullFillSingleRestingOrder() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 150));
        // Test case shows the matchable quantity of the order being used rather than the peak size/available qty
        Order incomingIcebergOrder = new IcebergOrder(1,2, Side.SELL, 100, 30);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingIcebergOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingIcebergOrder, 100);

        assertEquals(1, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertEquals(100, tradeConfirmations.iterator().next().getFilledQuantity());
        assertEquals(0, incomingIcebergOrder.getMatchableQuantity());
    }

    @Test
    public void testIncomingOrderFullFillMultipleRestingOrdersSamePriceLevel() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        buyBook.addToBook(new LimitOrder(3,3, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 150);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingLimitOrder, 100);

        TradeConfirmation expectedTC2 = new TradeConfirmation(3, incomingLimitOrder, 50);

        Iterator<TradeConfirmation> it = tradeConfirmations.iterator();
        assertEquals(2, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertTrue(tradeConfirmations.contains(expectedTC2));
        assertEquals(100, it.next().getFilledQuantity());
        assertEquals(50, it.next().getFilledQuantity());
        assertEquals(0, incomingLimitOrder.getMatchableQuantity());
    }

    @Test
    public void testIncomingOrderFullFillMultiplePriceLevels() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        buyBook.addToBook(new LimitOrder(3,4, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 150);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(3, incomingLimitOrder, 100);
        TradeConfirmation expectedTC2 = new TradeConfirmation(2, incomingLimitOrder, 50);

        Iterator<TradeConfirmation> it = tradeConfirmations.iterator();
        assertEquals(2, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertTrue(tradeConfirmations.contains(expectedTC2));
        assertEquals(100, it.next().getFilledQuantity());
        assertEquals(50, it.next().getFilledQuantity());
        assertEquals(0, incomingLimitOrder.getMatchableQuantity());
    }

    @Test
    public void testIncomingOrderPartialFillSingleRestingOrder() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 150);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingLimitOrder, 100);

        assertEquals(1, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertEquals(100, tradeConfirmations.iterator().next().getFilledQuantity());
        assertEquals(50, incomingLimitOrder.getMatchableQuantity());
    }

    @Test
    public void testIcebergIncomingOrderPartialFillSingleRestingOrder() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        // Test case shows the matchable quantity of the order being used rather than the peak size/available qty
        Order incomingIcebergOrder = new IcebergOrder(1,2, Side.SELL, 160, 30);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingIcebergOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingIcebergOrder, 100);

        assertEquals(1, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertEquals(100, tradeConfirmations.iterator().next().getFilledQuantity());
        assertEquals(60, incomingIcebergOrder.getMatchableQuantity());
        assertEquals(30, incomingIcebergOrder.getAvailableQuantity());
    }

    @Test
    public void testIncomingOrderFullPartialMultipleRestingOrdersSamePriceLevel() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        buyBook.addToBook(new LimitOrder(3,3, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 240);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingLimitOrder, 100);
        TradeConfirmation expectedTC2 = new TradeConfirmation(3, incomingLimitOrder, 100);

        Iterator<TradeConfirmation> it = tradeConfirmations.iterator();
        assertEquals(2, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertTrue(tradeConfirmations.contains(expectedTC2));
        assertEquals(100, it.next().getFilledQuantity());
        assertEquals(100, it.next().getFilledQuantity());
        assertEquals(40, incomingLimitOrder.getMatchableQuantity());
    }

    @Test
    public void testIncomingOrderPartialFillMultiplePriceLevels() {
        buyBook.addToBook(new LimitOrder(2,3, Side.BUY, 100));
        buyBook.addToBook(new LimitOrder(3,4, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL,
                280);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingLimitOrder, 100);
        TradeConfirmation expectedTC2 = new TradeConfirmation(3, incomingLimitOrder, 100);

        Iterator<TradeConfirmation> it = tradeConfirmations.iterator();
        assertEquals(2, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertTrue(tradeConfirmations.contains(expectedTC2));
        assertEquals(100, it.next().getFilledQuantity());
        assertEquals(100, it.next().getFilledQuantity());
        assertEquals(80, incomingLimitOrder.getMatchableQuantity());
    }

    @Test
    public void testTradeConfirmationsWithIcebergRestingOrderFills() {
        buyBook.addToBook(new IcebergOrder(2,3, Side.BUY, 100, 30));
        buyBook.addToBook(new LimitOrder(3,3, Side.BUY, 20));
        buyBook.addToBook(new LimitOrder(4,3, Side.BUY, 30));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 81);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        // Filled quantity of 31 shows aggregated iceberg fills + rotation
        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingLimitOrder, 31);
        TradeConfirmation expectedTC2 = new TradeConfirmation(3, incomingLimitOrder, 20);
        TradeConfirmation expectedTC3 = new TradeConfirmation(4, incomingLimitOrder, 30);

        // assert trade confirmations
        Iterator<TradeConfirmation> it = tradeConfirmations.iterator();
        assertEquals(3, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertTrue(tradeConfirmations.contains(expectedTC2));
        assertTrue(tradeConfirmations.contains(expectedTC3));
        assertEquals(31, it.next().getFilledQuantity());
        assertEquals(20, it.next().getFilledQuantity());
        assertEquals(30, it.next().getFilledQuantity());
    }

    @Test
    public void testIcebergRestingOrderPeakFillWithRotation() {
        buyBook.addToBook(new IcebergOrder(2,3, Side.BUY, 100, 30));
        buyBook.addToBook(new LimitOrder(3,3, Side.BUY, 100));
        Order incomingLimitOrder = new LimitOrder(1,2, Side.SELL, 80);
        Collection<TradeConfirmation> tradeConfirmations = matchingEngine.matchIncomingOrder(incomingLimitOrder, buyBook);

        // Iceberg order available quantity (peak) is filled
        TradeConfirmation expectedTC = new TradeConfirmation(2, incomingLimitOrder, 30);

        // Resting limit order fill after iceberg order in the order queue
        TradeConfirmation expectedTC2 = new TradeConfirmation(3, incomingLimitOrder, 50);

        // assert trade confirmations
        Iterator<TradeConfirmation> it = tradeConfirmations.iterator();
        assertEquals(2, tradeConfirmations.size());
        assertTrue(tradeConfirmations.contains(expectedTC));
        assertTrue(tradeConfirmations.contains(expectedTC2));
        assertEquals(30, it.next().getFilledQuantity());
        assertEquals(50, it.next().getFilledQuantity());

        List<Order> buyBookOrders = buyBook.getOrders();

        assertEquals(2, buyBookOrders.size());

        // Assert book size & order sequence in the order book post fills
        assertEquals(2, buyBookOrders.size());
        assertEquals(3, buyBookOrders.getFirst().getOrderId());
        assertEquals(2, buyBookOrders.get(1).getOrderId());

        // Assert iceberg order values which tests refill
        Order icebergOrderFromBook = buyBookOrders.get(1);
        assertEquals(3, icebergOrderFromBook.getPrice());
        assertEquals(30, icebergOrderFromBook.getAvailableQuantity());
        assertEquals(70, icebergOrderFromBook.getMatchableQuantity());
    }

}
