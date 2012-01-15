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
