package com.mamba.benchmark.common.pressure;

public class Custom extends Pressure {

    private final int[] quantities;

    private final int durationPerStep;

    public Custom(int[] quantities, int durationPerStep) {
        if (quantities == null || quantities.length == 0) {
            throw new IllegalArgumentException("Empty quantities");
        }
        for (long quantity : quantities) {
            if (quantity < 1) {
                throw new IllegalArgumentException("Invalid quantity: " + quantity);
            }
        }
        //每阶段不小于10，总时长不小于60
        if (durationPerStep < 10 || durationPerStep * quantities.length < 60) {
            throw new IllegalArgumentException("Invalid durationPerStep: " + durationPerStep);
        }
        this.quantities = quantities;
        this.durationPerStep = durationPerStep;
    }

    @Override
    protected int getQuantity(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        int idx = offset / this.durationPerStep;
        if (idx >= this.quantities.length) {
            return -1;
        }
        return this.quantities[idx];
    }
}
