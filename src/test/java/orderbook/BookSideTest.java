package orderbook;

import engine.MatchingEngine;
import model.LimitOrder;
import model.Order;
import model.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookSideTest {

    private BookSide sellBook;

    @BeforeEach
    void setUp() {
        sellBook = new BookSide(
                (incomingPrice, bestBuyPrice) -> incomingPrice >= bestBuyPrice,
                (incomingPrice, currentPrice) -> incomingPrice < currentPrice);
    }

    @Test
    public void testAddToEmptyBook() {
        Order order = new LimitOrder(1,2, Side.SELL, 100);
        sellBook.addToBook(order);
        assertEquals(1,sellBook.getOrders().size());
    }

    @Test
    public void testAddMultipleOrdersSamePriceToBook() {
        Order order = new LimitOrder(1,3, Side.SELL, 100);
        Order order2 = new LimitOrder(2,3, Side.SELL, 200);
        Order order3 = new LimitOrder(3,3, Side.SELL, 300);
        sellBook.addToBook(order);
        sellBook.addToBook(order2);
        sellBook.addToBook(order3);
        assertEquals(3,sellBook.getOrders().size());
    }

    @Test
    public void testAddMultipleOrdersDifferentPricesToBook() {
        Order order = new LimitOrder(1,4, Side.SELL, 100);
        Order order2 = new LimitOrder(2,3, Side.SELL, 200);
        Order order3 = new LimitOrder(3,5, Side.SELL, 300);
        sellBook.addToBook(order);
        sellBook.addToBook(order2);
        sellBook.addToBook(order3);
        assertEquals(3,sellBook.getOrders().size());
        assertEquals(2,sellBook.getOrders().getFirst().getOrderId());
        assertEquals(1,sellBook.getOrders().get(1).getOrderId());
        assertEquals(3,sellBook.getOrders().get(2).getOrderId());
    }

    @Test
    public void testAddToBookWithZeroAvailableQuantity() {
        Order order = new LimitOrder(3,2, Side.BUY, 0);
        sellBook.addToBook(order);
        assertEquals(0,sellBook.getOrders().size());
    }

    @Test
    public void testRemoveHeadFromBook() {
        // should not throw an exception if book empty
        sellBook.removeHead();

        // two standard orders in price order
        Order order = new LimitOrder(1,3, Side.SELL, 100);
        Order order2 = new LimitOrder(2,4, Side.SELL, 200);
        sellBook.addToBook(order);
        sellBook.addToBook(order2);

        sellBook.removeHead();
        assertEquals(1,sellBook.getOrders().size());
        assertEquals(2,sellBook.getOrders().getFirst().getOrderId());

        // add order with a better price than existing orders then remove head (should remove the newly added order)
        order = new LimitOrder(3,2, Side.SELL, 80);
        sellBook.addToBook(order);

        sellBook.removeHead();
        assertEquals(1,sellBook.getOrders().size());
        assertEquals(2,sellBook.getOrders().getFirst().getOrderId());
    }

    @Test
    public void TestBestMatchingPriceLevelEmptyBook() {
        PriceLevel pl = sellBook.bestMatchingPriceLevel(10);
        assertNull(pl);
    }

    @Test
    public void TestBestMatchingPriceLevelWithNoMatch() {
        Order order = new LimitOrder(1,5, Side.SELL, 100);
        sellBook.addToBook(order);
        PriceLevel pl = sellBook.bestMatchingPriceLevel(4);
        assertNull(pl);
    }

    @Test
    public void TestBestMatchingPriceLevelWithMatch() {
        Order order = new LimitOrder(1,5, Side.SELL, 100);
        sellBook.addToBook(order);
        PriceLevel priceLevel = sellBook.bestMatchingPriceLevel(6);
        assertEquals(5, priceLevel.getPrice());
    }

    @Test
    public void TestBestMatchingPriceLevelWithMatchMultiplePriceLevels() {
        Order order = new LimitOrder(1,4, Side.SELL, 100);
        Order order2 = new LimitOrder(2,3, Side.SELL, 200);
        Order order3 = new LimitOrder(3,5, Side.SELL, 300);
        sellBook.addToBook(order);
        sellBook.addToBook(order2);
        sellBook.addToBook(order3);
        PriceLevel priceLevel = sellBook.bestMatchingPriceLevel(6);
        assertEquals(3, priceLevel.getPrice());
    }

    @Test
    public void testGetOrderEmptyBook() {
        List<Order> orders = sellBook.getOrders();
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetOrderSinglePriceLevel() {
        Order order = new LimitOrder(1,4, Side.SELL, 100);
        Order order2 = new LimitOrder(2,4, Side.SELL, 200);
        sellBook.addToBook(order);
        sellBook.addToBook(order2);

        List<Order> orders = sellBook.getOrders();
        assertEquals(2, orders.size());
        assertEquals(1, orders.getFirst().getOrderId());
        assertEquals(2, orders.get(1).getOrderId());
    }

    @Test
    public void testGetOrderMultiplePriceLevels() {
        Order order = new LimitOrder(1,4, Side.SELL, 100);
        Order order2 = new LimitOrder(2,4, Side.SELL, 200);
        Order order3 = new LimitOrder(3,3, Side.SELL, 200);
        sellBook.addToBook(order);
        sellBook.addToBook(order2);
        sellBook.addToBook(order3);

        List<Order> orders = sellBook.getOrders();
        assertEquals(3, orders.size());
        assertEquals(3, orders.getFirst().getOrderId());
        assertEquals(1, orders.get(1).getOrderId());
        assertEquals(2, orders.get(2).getOrderId());
    }



}
