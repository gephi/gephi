/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.timeline;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 * @author mbastian
 */
public class RealTick {

    //Consts
    protected final static int MIN_PIXELS = 10;
    //Fields
    private final DecimalFormat decimalFormat;
    private final double min;
    private final double max;
    private final double minTick;
    private final double maxTick;
    private final double tick;
    private final int exponent;

    public RealTick(double min, double max, double minTick, double maxTick, double tick, int exponent) {
        decimalFormat = new DecimalFormat();
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        this.min = min;
        this.max = max;
        this.minTick = minTick;
        this.maxTick = maxTick;
        this.tick = tick;
        this.exponent = exponent;
    }

    public static RealTick create(double min, double max, int width) {
        double diff = Math.abs(max - min);
        double minTick = diff / 100.0;
        int exponent = (int) Math.round(Math.log10(minTick));

        int minTickPixels = RealTick.MIN_PIXELS;
        int tickPixels;
        double tick;
        int mult = 1;
        do {
            tick = exponent > 0 ? Math.pow(10, Math.abs(exponent)) : 1.0 / Math.pow(10, Math.abs(exponent));
            tick *= mult;
            double countTicks = diff / tick;
            tickPixels = (int) Math.round(width / countTicks);
            if (mult == 1) {
                mult = 5;
            } else {
                exponent++;
                mult = 1;
            }
        } while (tickPixels < minTickPixels);

        double minStart = (int) (min / tick) * tick;
        double maxEnd = (int) (Math.ceil(max / tick)) * tick;

        return new RealTick(min, max, minStart, maxEnd, tick, exponent);
    }

    public int getNumberTicks() {
        return (int) (Math.abs(maxTick - minTick) / tick);
    }

    public double getTickPosition(int index) {
        return (index * tick - (min - minTick)) / (max - min);
    }

    public int getTickRank(int index) {
        double t = minTick + (index * tick);
        t = round(t, exponent >= 0 ? 1 : Math.abs(exponent));
        if (t % (tick * 10) == 0) {
            return 2;
        } else if (t % (tick * 5) == 0) {
            return 1;
        }
        return 0;
    }

    public String getTickValue(int index) {
        double val = minTick + tick * index;

        decimalFormat.setMaximumFractionDigits(Math.abs(exponent) + 1);
        return decimalFormat.format(val);
    }

    public int getTickPixelPosition(int index, int width) {
        double pos = getTickPosition(index);
        return (int) (width * pos);
    }

    public double getMax() {
        return max;
    }

    public double getMaxTick() {
        return maxTick;
    }

    public double getMin() {
        return min;
    }

    public double getMinTick() {
        return minTick;
    }

    public double getTick() {
        return tick;
    }

    public int getExponent() {
        return exponent;
    }

    private double round(double valueToRound, int numberOfDecimalPlaces) {
        double multipicationFactor = Math.pow(10, numberOfDecimalPlaces);
        double interestedInZeroDPs = valueToRound * multipicationFactor;
        return Math.round(interestedInZeroDPs) / multipicationFactor;
    }
}