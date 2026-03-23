package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IcebergOrderTest {
    private IcebergOrder icebergOrder;

    @BeforeEach
    void setUp() {
        icebergOrder = new IcebergOrder(1,2, Side.BUY, 8, 5);
    }

    @Test
    void testVerifyIcebergInstanceFields() {
        assertEquals(1, icebergOrder.getOrderId());
        assertEquals(2, icebergOrder.getPrice());
        assertEquals(Side.BUY, icebergOrder.getSide());
        assertEquals(5, icebergOrder.getPeakSize());
        assertEquals(5, icebergOrder.getAvailableQuantity());
        assertEquals(3, icebergOrder.getRemainingQuantity());
    }

    @Test
    void testPeakSizeIfExceedsTotalQuantity() {
        IcebergOrder icebergOrder1 = new IcebergOrder(2,3,Side.SELL, 10, 500);
        assertEquals(10, icebergOrder1.getPeakSize());
        assertEquals(10, icebergOrder1.getAvailableQuantity());
        assertEquals(0, icebergOrder1.getRemainingQuantity());
    }

    @Test
    void testGetMatchableQuantity() {
        assertEquals(8, icebergOrder.getMatchableQuantity());
        assertEquals(5,icebergOrder.getAvailableQuantity());
        assertEquals(3,icebergOrder.getRemainingQuantity());

        // check matchable quantity post fill
        icebergOrder.fill(5);
        assertEquals(3, icebergOrder.getMatchableQuantity());
        assertEquals(0,icebergOrder.getAvailableQuantity());
        assertEquals(3,icebergOrder.getRemainingQuantity());
    }

    @Test
    void testFillAvailableQuantity() {
        // Fill less than available quantity
        icebergOrder.fill(1);
        assertEquals(4, icebergOrder.getAvailableQuantity());
        assertEquals(3, icebergOrder.getRemainingQuantity());

        // Fill exact available quantity
        icebergOrder.fill(4);
        assertEquals(0, icebergOrder.getAvailableQuantity());
        assertEquals(3, icebergOrder.getRemainingQuantity());

        // Fill more than available quantity
        icebergOrder = new IcebergOrder(2,3, Side.BUY, 8, 5);
        icebergOrder.fill(8);
        assertEquals(0, icebergOrder.getAvailableQuantity());
        assertEquals(0, icebergOrder.getRemainingQuantity());
    }

    @Test
    void testRefillAvailableQuantity() {
        // No fills yet - available quantity = 5 so stays as is
        icebergOrder.refillAvailableQuantity();
        assertEquals(5, icebergOrder.getAvailableQuantity());
        assertEquals(3, icebergOrder.getRemainingQuantity());

        // Fill available quantity & refill
        icebergOrder.fill(5);
        icebergOrder.refillAvailableQuantity();
        assertEquals(3, icebergOrder.getAvailableQuantity());
        assertEquals(0, icebergOrder.getRemainingQuantity());

        // Fill available quantity and refill
        icebergOrder.fill(3);
        icebergOrder.refillAvailableQuantity();
        assertEquals(0, icebergOrder.getAvailableQuantity()); // no refill expected as remainingQty = 0
        assertEquals(0, icebergOrder.getRemainingQuantity());
    }

    @Test
    void testIcebergOrderPostFillActions() {
        // Partial fill
        icebergOrder.fill(3);
        assertEquals(2, icebergOrder.getAvailableQuantity());
        assertEquals(3, icebergOrder.getRemainingQuantity());
        assertEquals(PostFillAction.KEEP, icebergOrder.postFill());

        // available quantity filled
        icebergOrder.fill(2);
        assertEquals(0, icebergOrder.getAvailableQuantity());
        assertEquals(3, icebergOrder.getRemainingQuantity());
        assertEquals(PostFillAction.ROTATE, icebergOrder.postFill());

        // Remaining quantity filled
        icebergOrder.fill(3);
        assertEquals(0, icebergOrder.getAvailableQuantity());
        assertEquals(0, icebergOrder.getRemainingQuantity());
        assertEquals(PostFillAction.REMOVE, icebergOrder.postFill());
    }
}
