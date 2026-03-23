package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LimitOrderTest {
    private LimitOrder limitOrder;

    @BeforeEach
    void setUp() {
        limitOrder = new LimitOrder(1,2, Side.BUY, 10);
    }

    @Test
    void testVerifyLimitInstanceFields() {
        assertEquals(1, limitOrder.getOrderId());
        assertEquals(2, limitOrder.getPrice());
        assertEquals(Side.BUY, limitOrder.getSide());
        assertEquals(10, limitOrder.getAvailableQuantity());
    }

    @Test
    void testFillReducesAvailableQuantity() {
        limitOrder.fill(5);
        assertEquals(5, limitOrder.getAvailableQuantity());
    }

    @Test
    void testGetMatchableQuantity() {
        assertEquals(10, limitOrder.getMatchableQuantity());

        // check matchable quantity post fill
        limitOrder.fill(5);
        assertEquals(5, limitOrder.getMatchableQuantity());
    }

    @Test
    void testLimitOrderPostFillActions() {
        assertEquals(PostFillAction.KEEP, limitOrder.postFill());

        limitOrder.fill(10);
        assertEquals(PostFillAction.REMOVE, limitOrder.postFill());
    }

    @Test
    void testNoOpOnRefillAvailableQuantity() {
        limitOrder.refillAvailableQuantity();
        assertEquals(10, limitOrder.getAvailableQuantity());

    }
}
