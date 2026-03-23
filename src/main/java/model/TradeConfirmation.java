package model;

import java.util.Objects;

public class TradeConfirmation {
    private final int restingOrderId;
    private final int incomingOrderId;
    private final Side incomingOrderSide;
    private final int incomingOrderPrice;
    private int filledQuantity;

    public TradeConfirmation(int restingOrderId, Order incomingOrder, int filledQuantity) {
        this.restingOrderId = restingOrderId;
        this.incomingOrderId = incomingOrder.getOrderId();
        this.incomingOrderSide = incomingOrder.getSide();
        this.incomingOrderPrice = incomingOrder.getPrice();
        this.filledQuantity = filledQuantity;
    }

    public int getRestingOrderId() {
        return this.restingOrderId;
    }

    public int getIncomingOrderId() {
        return this.incomingOrderId;
    }

    public Side getIncomingOrderSide(){
        return this.incomingOrderSide;
    }

    public int getIncomingOrderPrice() {
        return this.incomingOrderPrice;
    }

    public int getFilledQuantity() {
        return this.filledQuantity;
    }

    public void updateFilledQuantity(int quantity) {
        this.filledQuantity += quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeConfirmation tc = (TradeConfirmation) o;
        return restingOrderId == tc.getRestingOrderId()
                &&  incomingOrderId == tc.getIncomingOrderId()
                && incomingOrderSide == tc.getIncomingOrderSide()
                && incomingOrderPrice == tc.getIncomingOrderPrice();
    }

    @Override
    public int hashCode() {
        return Objects.hash(restingOrderId, incomingOrderId, incomingOrderSide, incomingOrderPrice);
    }

}
