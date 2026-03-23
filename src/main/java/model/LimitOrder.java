package model;

public class LimitOrder extends Order {

    public LimitOrder(final int orderId, final int price, final Side side, int availableQuantity) {
        super(orderId, price, side, availableQuantity);
    }

    @Override
    public void fill(int quantity) {
        availableQuantity -= quantity;
    }

    @Override
    public int getMatchableQuantity() {
        return availableQuantity;
    }
}
