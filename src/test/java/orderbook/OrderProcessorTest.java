package orderbook;

import model.LimitOrder;
import model.Order;
import model.Side;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderProcessorTest {
    OrderProcessor orderProcessor;

    @BeforeEach
    public void setup() {
        orderProcessor = new OrderProcessor();
    }

    @Test
    public void testProcessOrderUniqueOrderIds() {
        Order incominglimitOrder = new LimitOrder(1,2, Side.BUY, 10);
        orderProcessor.processOrder(incominglimitOrder);
        assertEquals(1, orderProcessor.getBuyOrders().size());

        incominglimitOrder = new LimitOrder(2,4, Side.BUY, 12);
        orderProcessor.processOrder(incominglimitOrder);
        assertEquals(2, orderProcessor.getBuyOrders().size());
    }

    @Test
    public void testProcessOrderDuplicateOrderIds() {
        Order incominglimitOrder = new LimitOrder(1,2, Side.BUY, 10);
        orderProcessor.processOrder(incominglimitOrder);
        assertEquals(1, orderProcessor.getBuyOrders().size());

        incominglimitOrder = new LimitOrder(1,4, Side.BUY, 12);
        orderProcessor.processOrder(incominglimitOrder);
        assertEquals(1, orderProcessor.getBuyOrders().size());
    }

    @Test
    public void testMatchIncomingBuyOrderPartialFill() {
        Order restingSellOrder = new LimitOrder(1,5, Side.SELL, 10);
        orderProcessor.processOrder(restingSellOrder);
        // Verify sell book contains one order
        assertEquals(1, orderProcessor.getSellOrders().size());

        Order incomingBuyOrder = new LimitOrder(2,6, Side.BUY, 100);
        orderProcessor.processOrder(incomingBuyOrder);
        // Verify buy book contains one order
        assertEquals(1, orderProcessor.getBuyOrders().size());

        // Verify SELL order removed from book post fill
        assertTrue(orderProcessor.getSellOrders().isEmpty());
    }

    @Test
    public void testMatchIncomingBuyOrderNoFill() {
        Order restingSellOrder = new LimitOrder(1,5, Side.SELL, 10);
        orderProcessor.processOrder(restingSellOrder);
        // Verify sell book contains one order
        assertEquals(1, orderProcessor.getSellOrders().size());

        Order incomingBuyOrder = new LimitOrder(2,4, Side.BUY, 100);
        orderProcessor.processOrder(incomingBuyOrder);

        // Verify BUY and SELL book contain one order post incoming buy order
        assertEquals(1, orderProcessor.getSellOrders().size());
        assertEquals(1, orderProcessor.getBuyOrders().size());
    }

    @Test
    public void testMatchIncomingBuyOrderFullFill() {
        Order restingSellOrder = new LimitOrder(1,5, Side.SELL, 100);
        orderProcessor.processOrder(restingSellOrder);
        // Verify sell book contains one order
        assertEquals(1, orderProcessor.getSellOrders().size());

        Order incomingBuyOrder = new LimitOrder(2,6, Side.BUY, 100);
        orderProcessor.processOrder(incomingBuyOrder);

        // Verify BUY and SELL book contain one order post incoming buy order
        assertTrue(orderProcessor.getSellOrders().isEmpty());
        assertTrue(orderProcessor.getBuyOrders().isEmpty());
    }
}
