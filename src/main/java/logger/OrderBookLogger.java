package logger;

import model.Order;
import model.Side;
import model.TradeConfirmation;

import java.util.Collection;
import java.util.List;

public class OrderBookLogger {

    public static void printTradeConfirmations(Collection<TradeConfirmation> tradeConfirmations) {
        for (TradeConfirmation tradeConfirmation : tradeConfirmations) {
            if (tradeConfirmation.getIncomingOrderSide() == Side.BUY) {
                System.out.printf("%d,%d,%d,%d%n", tradeConfirmation.getIncomingOrderId(), tradeConfirmation.getRestingOrderId(), tradeConfirmation.getIncomingOrderPrice(), tradeConfirmation.getFilledQuantity());
            } else {
                System.out.printf("%d,%d,%d,%d%n", tradeConfirmation.getRestingOrderId(), tradeConfirmation.getIncomingOrderId(), tradeConfirmation.getIncomingOrderPrice(), tradeConfirmation.getFilledQuantity());
            }
        }
    }

    public static void printBookSummary(List<Order> buyOrders, List<Order> sellOrders) {

        int buyOrderSize = buyOrders.size();
        int sellOrderSize = sellOrders.size();

        System.out.println("+-----------------------------------------------------------------+");
        System.out.println("| BUY                            | SELL                           |");
        System.out.println("| Id       | Volume      | Price | Price | Volume      | Id       |");
        System.out.println("+----------+-------------+-------+-------+-------------+----------+");

        for (int i = 0; i < Math.max(buyOrderSize, sellOrderSize); i++) {
            Order buyOrder = i < buyOrderSize ? buyOrders.get(i) : null;
            Order sellOrder = i < sellOrderSize ? sellOrders.get(i) : null;
            printRow(buyOrder,sellOrder);
        }

        System.out.println("+-----------------------------------------------------------------+");
    }

    private static void printRow(Order buyOrder, Order sellOrder) {
        if (buyOrder != null && sellOrder != null) {
            System.out.printf("| %8d | %,11d | %5d | %5d | %,11d | %8d |%n",
                    buyOrder.getOrderId(), buyOrder.getAvailableQuantity(), buyOrder.getPrice(),
                    sellOrder.getPrice(), sellOrder.getAvailableQuantity(), sellOrder.getOrderId());
        } else if (buyOrder != null) {
            System.out.printf("| %8d | %,11d | %5d |       |             |          |%n",
                    buyOrder.getOrderId(), buyOrder.getAvailableQuantity(), buyOrder.getPrice());
        } else {
            System.out.printf("|          |             |       | %5d | %,11d | %8d |%n",
                    sellOrder.getPrice(), sellOrder.getAvailableQuantity(), sellOrder.getOrderId());
        }
    }
}
