package parser;

import model.IcebergOrder;
import model.LimitOrder;
import model.Order;
import model.Side;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class OrderParser {
    private static final String BUY_FLAG = "B";
    private static final Set<String> VALID_SIDES = Set.of(BUY_FLAG, "S");

    public static List<Order> getOrdersFromInputLines(List<String> inputLines) {
        List<Order> orders = new ArrayList<>();
        inputLines.forEach(inputLine -> extractOrderFromInputLine(inputLine).ifPresent(orders::add));
        return orders;
    }

    /**
     * Parsing individual components from the order details read from file.
     * Spec doesn't specify any constraints on the possible values of an orderId, so have left orderId unconstrained.
     *
     * @param inputLine
     * @return
     */
    private static Optional<Order> extractOrderFromInputLine(String inputLine) {
        String[] lineSplit = inputLine.split(",");

        // validate input length
        if (!(lineSplit.length == 4 || lineSplit.length == 5)) return Optional.empty();

        // validate first argument - side
        if (!VALID_SIDES.contains(lineSplit[0])) return Optional.empty();
        Side side = BUY_FLAG.equals(lineSplit[0]) ? Side.BUY : Side.SELL;

        try {
            int orderId = Integer.parseInt(lineSplit[1]);
            int price = Integer.parseInt(lineSplit[2]);
            int quantity = Integer.parseInt(lineSplit[3]);

            if (price <= 0 || quantity <= 0) return Optional.empty();

            if (lineSplit.length == 4) {
                return Optional.of(new LimitOrder(orderId, price, side, quantity));
            } else {
                int peakSize = Integer.parseInt(lineSplit[4]);
                if (peakSize <= 0) return Optional.empty();
                return Optional.of(new IcebergOrder(orderId, price, side, quantity, peakSize));
            }
        } catch (NumberFormatException e) {
            // ignore failed parsed lines silently as per spec
        }

        return Optional.empty();
    }
}
