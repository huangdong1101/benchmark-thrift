package com.mamba.benchmark.common.pressure;

public class Gradient extends Pressure {

    private final int initialQuantity;

    private final int incrementPerStep;

    private final int increaseSteps;

    private final int durationPerStep;

    public Gradient(int initialQuantity, int incrementPerStep, int increaseSteps, int durationPerStep) {
        if (initialQuantity < 1) {
            throw new IllegalArgumentException("Invalid initialQuantity: " + initialQuantity);
        }
        if (increaseSteps < 1) {
            throw new IllegalArgumentException("Invalid increaseSteps: " + increaseSteps);
        }
        if (incrementPerStep < 1) {
            throw new IllegalArgumentException("Invalid incrementPerStep: " + incrementPerStep);
        }
        if (durationPerStep < 10) {
            throw new IllegalArgumentException("Invalid durationPerStep: " + durationPerStep);
        }
        this.initialQuantity = initialQuantity;
        this.incrementPerStep = incrementPerStep;
        this.increaseSteps = increaseSteps;
        this.durationPerStep = durationPerStep;
    }

    @Override
    protected int getQuantity(int offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("Invalid offset: " + offset);
        }
        int idx = offset / this.durationPerStep;
        if (idx == 0) {
            return this.initialQuantity;
        }
        if (idx <= this.increaseSteps) {
            return this.initialQuantity + this.incrementPerStep * idx;
        }
        return -1;
    }
}
