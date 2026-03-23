package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TradeConfirmationTest {

    @Test
    public void testUpdateFilledQuantity() {
        Order incomingOrder = new LimitOrder(2,3, Side.BUY, 150);
        TradeConfirmation tradeConfirmation = new TradeConfirmation(3, incomingOrder, 100);
        assertEquals(100, tradeConfirmation.getFilledQuantity());

        tradeConfirmation.updateFilledQuantity(55);
        assertEquals(155, tradeConfirmation.getFilledQuantity());
    }

    @Test
    public void testEqualsOverride() {
        Order incomingOrder = new LimitOrder(2,3, Side.BUY, 150);
        TradeConfirmation tradeConfirmation = new TradeConfirmation(3, incomingOrder, 100);
        TradeConfirmation tradeConfirmation2 = new TradeConfirmation(3, incomingOrder, 50);

        assertEquals(tradeConfirmation, tradeConfirmation2);

        // different restingOrder id
        tradeConfirmation2 = new TradeConfirmation(4, incomingOrder, 50);
        assertNotEquals(tradeConfirmation, tradeConfirmation2);

        // same resting order id, different incoming order object
        Order incomingOrder2 = new LimitOrder(2,4, Side.BUY, 150);
        tradeConfirmation2 = new TradeConfirmation(2, incomingOrder2, 50);
        assertNotEquals(tradeConfirmation, tradeConfirmation2);
    }

    @Test
    public void testEqualWithHashCodes() {
        Order incomingOrder = new LimitOrder(2,3, Side.BUY, 150);
        TradeConfirmation tradeConfirmation = new TradeConfirmation(3, incomingOrder, 100);
        TradeConfirmation tradeConfirmation2 = new TradeConfirmation(3, incomingOrder, 100);

        assertEquals(tradeConfirmation, tradeConfirmation2);
        assertEquals(tradeConfirmation.hashCode(), tradeConfirmation2.hashCode()); // verify hashcode match

        // change filledQuantity
        tradeConfirmation2 = new TradeConfirmation(3, incomingOrder, 123456);
        assertEquals(tradeConfirmation, tradeConfirmation2);
        assertEquals(tradeConfirmation.hashCode(), tradeConfirmation2.hashCode()); // verify hashcode match
    }
}
