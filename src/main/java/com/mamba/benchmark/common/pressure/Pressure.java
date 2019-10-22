package com.mamba.benchmark.common.pressure;

import com.google.common.base.Splitter;

import java.util.stream.StreamSupport;

public abstract class Pressure {

    private transient volatile long beginTime = 0;

    private long getBeginTime() {
        if (this.beginTime == 0) {
            synchronized (this) {
                if (this.beginTime == 0) {
                    this.beginTime = System.currentTimeMillis();
                }
            }
        }
        return beginTime;
    }

    public int currentQuantity() {
        long beginTime = this.getBeginTime();
        long currentTime = System.currentTimeMillis();
        return this.getQuantity((int) ((currentTime - beginTime) / 1000));
    }

    protected abstract int getQuantity(int offset);

    public static Pressure parse(String str, int duration) {
        if (str.indexOf(',') >= 0) {
            int[] quantities = StreamSupport.stream(Splitter.on(',').split(str).spliterator(), false)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();
            // -c 10,20,30
            return new Custom(quantities, duration);
        }
        int aIdx = str.indexOf('+');
        if (aIdx < 1) {
            return new Fixed(Integer.parseInt(str.trim()), duration);
        }
        int pIdx = str.indexOf('*', aIdx);
        if (pIdx > 0) {
            return new Gradient(
                    Integer.parseInt(str.substring(0, aIdx).trim()),
                    Integer.parseInt(str.substring(aIdx + 1, pIdx).trim()),
                    Integer.parseInt(str.substring(pIdx + 1).trim()),
                    duration);
        }
        throw new IllegalArgumentException("Invalid pressure: " + str);
    }
}
