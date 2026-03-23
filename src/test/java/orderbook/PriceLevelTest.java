package orderbook;

import model.IcebergOrder;
import model.LimitOrder;
import model.Order;
import model.Side;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PriceLevelTest {

    /**
     *  Entities:
     *      price
     *      queue of orders
     */

    @Test
    public void testAddOrder() {
        Order order = new LimitOrder(1,4, Side.SELL, 100);
        PriceLevel pl = new PriceLevel(order);
        assertEquals(4, pl.getPrice());
        assertEquals(1, pl.getOrders().size());
    }

    @Test
    public void testAddMultipleOrders() {
        Order order = new LimitOrder(1,3, Side.SELL, 100);
        Order order2 = new LimitOrder(2,3, Side.SELL, 100);
        PriceLevel pl = new PriceLevel(order);
        pl.add(order2);
        assertEquals(3, pl.getPrice());

        // assert order queue size and order sequence
        List<Order> orders = List.copyOf(pl.getOrders());

        assertEquals(2, orders.size());
        assertEquals(1,orders.getFirst().getOrderId());
        assertEquals(2,orders.get(1).getOrderId());
    }

    @Test
    public void testPeekFirst() {
        Order order = new LimitOrder(1,3, Side.SELL, 100);
        PriceLevel pl = new PriceLevel(order);
        Order orderFromBook = pl.peekFirst();
        assertEquals(1,orderFromBook.getOrderId());
        assertEquals(3,orderFromBook.getPrice());
    }

    @Test
    public void testRotateHead() {
        // Check rotate with single order present first
        Order icebergOrder = new IcebergOrder(1,3, Side.SELL, 100, 10);
        PriceLevel pl = new PriceLevel(icebergOrder);

        List<Order> orders = List.copyOf(pl.getOrders());
        assertEquals(1, orders.size());
        assertEquals(1, orders.getFirst().getOrderId());

        // add second order & initiate rotation
        Order icebergOrder1 = new IcebergOrder(2,3, Side.SELL, 100, 10);
        pl.add(icebergOrder1);
        pl.rotateHead();

        orders = List.copyOf(pl.getOrders());
        assertEquals(2, orders.size());
        assertEquals(2, orders.getFirst().getOrderId());
        assertEquals(1, orders.get(1).getOrderId());
    }

    @Test
    public void testRemoveHeadAndIsEmpty() {
        Order icebergOrder = new IcebergOrder(1,3, Side.SELL, 100, 10);
        PriceLevel pl = new PriceLevel(icebergOrder);
        pl.removeHead();

        Collection<Order> orders = pl.getOrders();
        assertEquals(0, orders.size());
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetPrice() {
        Order icebergOrder = new IcebergOrder(1,3, Side.SELL, 100, 10);
        PriceLevel pl = new PriceLevel(icebergOrder);
        assertEquals(3, pl.getPrice());
    }

    @Test
    public void testGetOrders() {
        Order icebergOrder = new IcebergOrder(1,3, Side.SELL, 100, 10);
        Order icebergOrder1 = new IcebergOrder(2,3, Side.SELL, 100, 10);
        PriceLevel pl = new PriceLevel(icebergOrder);
        pl.add(icebergOrder1);

        List<Order> orders = List.copyOf(pl.getOrders());
        assertEquals(2, orders.size());
    }
}
