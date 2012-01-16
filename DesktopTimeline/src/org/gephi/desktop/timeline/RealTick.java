/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.timeline;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 * @author Mathieu Bastian
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

    /**
     * Return 2 if multiple of 10, 1 if multiple of 5, 0 otherwise 
     */
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