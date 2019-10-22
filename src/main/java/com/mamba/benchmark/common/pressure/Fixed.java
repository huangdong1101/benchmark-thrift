package com.mamba.benchmark.common.pressure;

public class Fixed extends Pressure {

    private final int quantity;

    private final int duration;

    public Fixed(int quantity, int duration) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Invalid quantity: " + quantity);
        }
        if (duration < 10) {
            throw new IllegalArgumentException("Invalid duration: " + duration);
        }
        this.quantity = quantity;
        this.duration = duration;
    }

    @Override
    protected int getQuantity(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        if (offset < this.duration) {
            return this.quantity;
        }
        return -1;
    }
}
