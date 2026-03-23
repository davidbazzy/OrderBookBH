package orderbook;

import model.Order;

import java.util.*;

public class PriceLevel {

    private final int price;
    private final Deque<Order> orderQueue = new ArrayDeque<>();

    public PriceLevel(Order order) {
        this.price = order.getPrice();
        orderQueue.add(order);
    }

    public void add(Order order) {
        orderQueue.add(order);
    }

    public Order peekFirst() {
        return orderQueue.peekFirst();
    }

    public void rotateHead() {
        orderQueue.addLast(orderQueue.removeFirst());
    }

    public void removeHead() {
        if (!orderQueue.isEmpty()) {
            orderQueue.removeFirst();
        }
    }

    public boolean isEmpty() {
        return orderQueue.isEmpty();
    }

    public int getPrice() {
        return price;
    }

    // Exposing a read-only view of orders at a given price level
    public Collection<Order> getOrders() {
        return List.copyOf(orderQueue);
    }

}
