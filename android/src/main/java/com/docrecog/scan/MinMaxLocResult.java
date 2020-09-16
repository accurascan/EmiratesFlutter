package com.docrecog.scan;

import org.opencv.core.Point;

public class MinMaxLocResult {

    int ratio;
    double maxval;
    double minval;
    Point minIdx;
    Point maxIdx;

    public MinMaxLocResult(int ratio, double maxval, double minval, Point minIdx, Point maxIdx) {
        this.ratio = ratio;
        this.maxval = maxval;
        this.minval = minval;
        this.minIdx = minIdx;
        this.maxIdx = maxIdx;
    }

    public int getRatio() {
        return ratio;
    }

    public MinMaxLocResult setRatio(int ratio) {
        this.ratio = ratio;
        return this;
    }

    public double getMaxval() {
        return maxval;
    }

    public MinMaxLocResult setMaxval(double maxval) {
        this.maxval = maxval;
        return this;
    }

    public double getMinval() {
        return minval;
    }

    public MinMaxLocResult setMinval(double minval) {
        this.minval = minval;
        return this;
    }

    public Point getMinIdx() {
        return minIdx;
    }

    public MinMaxLocResult setMinIdx(Point minIdx) {
        this.minIdx = minIdx;
        return this;
    }

    public Point getMaxIdx() {
        return maxIdx;
    }

    public MinMaxLocResult setMaxIdx(Point maxIdx) {
        this.maxIdx = maxIdx;
        return this;
    }
}
