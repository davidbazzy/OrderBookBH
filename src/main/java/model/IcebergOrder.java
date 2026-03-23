package model;

public class IcebergOrder extends Order {

    private int remainingQuantity;
    private int peakSize;

    public IcebergOrder(final int orderId, final int price, final Side side, int totalQuantity, int peakSize) {
        super(orderId, price, side, Math.min(peakSize, totalQuantity));
        this.peakSize = Math.min(peakSize, totalQuantity);
        this.remainingQuantity = totalQuantity - availableQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public int getPeakSize() {
        return peakSize;
    }

    @Override
    public void fill(int quantityToFill) {
        int availableQuantityToFill = Math.min(quantityToFill, availableQuantity);
        availableQuantity -= availableQuantityToFill;
        remainingQuantity -= quantityToFill - availableQuantityToFill; // Remove left over quantityToFill from remaining quantity
    }

    @Override
    public int getMatchableQuantity() {
        return availableQuantity + remainingQuantity;
    }

    @Override
    public void refillAvailableQuantity() {
        if (availableQuantity == 0 && remainingQuantity > 0) {
            int refill = Math.min(peakSize, remainingQuantity);
            availableQuantity = refill;
            remainingQuantity -= refill;
        }
    }

    @Override
    public PostFillAction postFill() {
        if (availableQuantity == 0 && remainingQuantity > 0) {
            return PostFillAction.ROTATE;
        }
        return availableQuantity == 0 ? PostFillAction.REMOVE : PostFillAction.KEEP;
    }
}
