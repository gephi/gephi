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
public class StartEndTick {
    
    private final DecimalFormat decimalFormat;
    private final int exponentMin;
    private final int exponentMax;
    private final double min;
    private final double max;
    
    public StartEndTick(double min, double max, int exponentMin, int exponentMax) {
        decimalFormat = new DecimalFormat();
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        this.min = min;
        this.max = max;
        this.exponentMin = exponentMin;
        this.exponentMax = exponentMax;
    }
    
    public static StartEndTick create(double min, double max) {
        int exponentMin = (int) Math.round(Math.log10(min));
        int exponentMax = (int) Math.round(Math.log10(max));
        
        return new StartEndTick(min, max, exponentMin, exponentMax);
    }
    
    public int getExponentMax() {
        return exponentMax;
    }
    
    public int getExponentMin() {
        return exponentMin;
    }
    
    public double getMax() {
        return max;
    }
    
    public double getMin() {
        return min;
    }
    
    public String getStartValue() {
        if (exponentMin > 0) {
            return String.valueOf((long) min);
        }
        decimalFormat.setMaximumFractionDigits(Math.abs(exponentMin) + 2);
        return decimalFormat.format(min);
    }
    
    public String getEndValue() {
        if (exponentMax > 0) {
            return String.valueOf((long) max);
        }
        decimalFormat.setMaximumFractionDigits(Math.abs(exponentMax) + 2);
        return decimalFormat.format(max);
    }
}
