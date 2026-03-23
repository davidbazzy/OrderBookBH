package parser;

import model.Order;
import model.Side;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderParserTest {

    @Test
    public void testGetOrdersFromInputLinesValidLimitOrder(){
        List<String> lines = List.of("B,1,99,50000");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertEquals(1, orders.getFirst().getOrderId());
        assertEquals(Side.BUY, orders.getFirst().getSide());
        assertEquals(99, orders.getFirst().getPrice());
        assertEquals(50000, orders.getFirst().getMatchableQuantity());
        assertEquals(50000, orders.getFirst().getAvailableQuantity());
    }

    @Test
    public void testGetOrdersFromInputLinesValidIcebergOrder(){
        List<String> lines = List.of("S,1,99,50000,200");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertEquals(1, orders.getFirst().getOrderId());
        assertEquals(Side.SELL, orders.getFirst().getSide());
        assertEquals(99, orders.getFirst().getPrice());
        assertEquals(50000, orders.getFirst().getMatchableQuantity());
        assertEquals(200, orders.getFirst().getAvailableQuantity());
    }

    @Test
    public void testGetOrdersFromInputLinesInvalidPeakSizeWhitespace(){
        List<String> lines = List.of("B,1,99,50000, 200");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetOrdersFromInputLinesInvalidLength(){
        List<String> lines = List.of("B,1,99", "B,1,99,22,11,4");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetOrdersFromInputLinesNonNumericIntegerFields(){
        List<String> lines = List.of("B,A,99,5,10", "B,1,C,5,10", "B,1,99,D,10", "B,1,99,5,E");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetOrdersFromInputLinesInvalidSide(){
        List<String> lines = List.of("b,1,99,50000", "s,1,99,50000", "c,1,99,50000");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetOrdersFromInputLinesInvalidPrices(){
        List<String> lines = List.of("B,1,0,50000", "s,1,-50,50000");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertTrue(orders.isEmpty());
    }

    @Test
    public void testGetOrdersFromInputLinesInvalidQuantities(){
        List<String> lines = List.of("B,1,10,0", "s,1,50,-50000");
        List<Order> orders = OrderParser.getOrdersFromInputLines(lines);
        assertTrue(orders.isEmpty());
    }
}
