/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.timeline;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.timeline.api.TimelineChart;

/**
 *
 * @author mbastian
 */
public class TimelineChartImpl implements TimelineChart {

    private final AttributeColumn column;
    private final Number[] x;
    private final Number[] y;
    private final Number minY;
    private final Number maxY;

    public TimelineChartImpl(AttributeColumn column, Number[] x, Number y[]) {
        this.column = column;
        this.x = x;
        this.y = y;
        this.minY = calculateMin(y);
        this.maxY = calculateMax(y);
    }

    @Override
    public Number[] getX() {
        return x;
    }

    @Override
    public Number[] getY() {
        return y;
    }

    @Override
    public Number getMinY() {
        return minY;
    }

    @Override
    public Number getMaxY() {
        return maxY;
    }

    @Override
    public AttributeColumn getColumn() {
        return column;
    }

    private Number calculateMin(Number[] yValues) {
        double min = yValues[0].doubleValue();
        for (Number d : yValues) {
            min = Math.min(min, d.doubleValue());
        }
        Number t = yValues[0];
        if (t instanceof Double) {
            return new Double(min);
        } else if (t instanceof Float) {
            return new Float(min);
        } else if (t instanceof Short) {
            return new Short((short) min);
        } else if (t instanceof Long) {
            return new Long((long) min);
        } else if (t instanceof BigInteger) {
            return new BigDecimal(min);
        }
        return min;
    }

    private Number calculateMax(Number[] yValues) {
        double max = yValues[0].doubleValue();
        for (Number d : yValues) {
            max = Math.max(max, d.doubleValue());
        }
        Number t = yValues[0];
        if (t instanceof Double) {
            return new Double(max);
        } else if (t instanceof Float) {
            return new Float(max);
        } else if (t instanceof Short) {
            return new Short((short) max);
        } else if (t instanceof Long) {
            return new Long((long) max);
        } else if (t instanceof BigInteger) {
            return new BigDecimal(max);
        }
        return max;
    }
}
