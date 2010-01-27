/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.type;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mathieu Bastian
 */
//Brute-force implementation
public final class TimeInterval {

    private final double[][] array;
    private final double min;
    private final double max;

    public TimeInterval(List<double[]> intervaList) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        array = new double[intervaList.size()][2];
        for (int i = 0; i < array.length; i++) {
            double[] slice = intervaList.get(i);
            array[i] = new double[]{slice[0], slice[1]};
            minimum = Math.min(minimum, slice[0]);
            maximum = Math.max(maximum, slice[1]);
        }
        min = minimum;
        max = maximum;
    }

    public TimeInterval(double[][] intervalArray) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        array = new double[intervalArray.length][2];
        for (int i = 0; i < array.length; i++) {
            double[] slice = intervalArray[i];
            array[i] = new double[]{slice[0], slice[1]};
            minimum = Math.min(minimum, slice[0]);
            maximum = Math.max(maximum, slice[1]);
        }
        min = minimum;
        max = maximum;
    }

    public TimeInterval(double start, double end) {
        array = new double[][]{{start, end}};
        min = start;
        max = end;
    }

    public TimeInterval(String str) {
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        if (str.charAt(0) == '[') {
            Pattern p = Pattern.compile("\\[([ 0-9.,]*)\\]");
            Matcher m = p.matcher(str);
            int count = 0;
            while (m.find()) {
                count++;
            }
            m.reset();
            array = new double[count][2];
            int i = 0;
            while (m.find()) {
                int start = m.start();
                int end = m.end();
                if (start != end) {
                    String data = str.substring(start + 1, end - 1).trim();
                    data = data.trim();
                    String[] split = data.split(",");
                    double begin = Double.parseDouble(split[0]);
                    double close = Double.parseDouble(split[1]);
                    array[i++] = new double[]{begin, close};
                    minimum = Math.min(minimum, begin);
                    maximum = Math.max(maximum, close);
                }
            }
        } else {
            String[] split = str.split(",");
            double[] slice = null;
            array = new double[split.length / 2][2];
            for (int i = 0; i < split.length; i++) {
                if (i % 2 == 0) {
                    slice = new double[2];
                    String s = split[i];
                    slice[0] = Double.parseDouble(s);
                    minimum = Math.min(minimum, slice[0]);
                } else {
                    String s = split[i];
                    slice[1] = Double.parseDouble(s);
                    maximum = Math.max(maximum, slice[1]);
                    array[i / 2] = slice;
                }
            }
        }

        min = minimum;
        max = maximum;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public int getSliceCount() {
        return array.length;
    }

    public double getStart(int sliceIndex) {
        return array[sliceIndex][0];
    }

    public double getEnd(int sliceIndex) {
        return array[sliceIndex][1];
    }

    public boolean isInRange(double start, double end) {
        for (int i = 0; i < array.length; i++) {
            double[] slice = array[i];
            if (slice[0] <= start && slice[1] >= end) {
                return true;
            }
            if (slice[0] >= start && slice[0] <= end) {
                return true;
            }
            if (slice[1] >= start && slice[1] <= end) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if (array.length > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < array.length; i++) {
                double[] slice = array[i];
                stringBuilder.append(slice[0]);
                stringBuilder.append(",");
                stringBuilder.append(slice[1]);
            }
            return stringBuilder.toString();
        } else {
            return array[0][0] + "," + array[0][1];
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof TimeInterval) {
            TimeInterval inter = (TimeInterval) obj;
            if (inter.array.length == array.length) {
                for (int i = 0; i < inter.array.length; i++) {
                    double[] slice = inter.array[i];
                    if (slice[0] != array[i][0]) {
                        return false;
                    }
                    if (slice[1] != array[i][1]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Arrays.deepHashCode(this.array);
        return hash;
    }
}
