package model;

public abstract class Order {
    private final int orderId;
    private final int price;
    private final Side side;
    int availableQuantity;

    protected Order(final int orderId, final int price, final Side side, int availableQuantity) {
        this.orderId = orderId;
        this.price = price;
        this.side = side;
        this.availableQuantity = availableQuantity;
    }

    public abstract void fill(int quantity);

    public abstract int getMatchableQuantity();

    public int getOrderId() {
        return orderId;
    }

    public int getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void refillAvailableQuantity() {
        // no-op by default
    }

    public PostFillAction postFill() {
        return availableQuantity == 0 ? PostFillAction.REMOVE : PostFillAction.KEEP;
    }
}
