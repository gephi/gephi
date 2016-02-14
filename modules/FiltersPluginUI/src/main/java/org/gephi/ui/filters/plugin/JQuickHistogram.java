/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.filters.plugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author Mathieu Bastian
 */
public class JQuickHistogram {

    private int constraintHeight = 0;
    private int constraintWidth = 0;
    private JPanel panel;
    private final boolean inclusive = true;
    //Data
    private List<Double> data;
    private Double minValue;
    private Double maxValue;
    private Double minRange;
    private Double maxRange;

    public JQuickHistogram() {
        clear();
    }

    public void clear() {
        data = new ArrayList<>();
        minValue = Double.MAX_VALUE;
        maxValue = Double.NEGATIVE_INFINITY;
    }

    public void addData(Object data) {
        if (data instanceof Double) {
            addData((Double) data);
        } else if (data instanceof Integer) {
            addData(((Integer) data).doubleValue());
        } else if (data instanceof Float) {
            addData(((Float) data).doubleValue());
        } else if (data instanceof Long) {
            addData(((Long) data).doubleValue());
        }
    }

    public void addData(Double data) {
        this.data.add(data);
        minValue = Math.min(minValue, data);
        maxValue = Math.max(maxValue, data);
        minRange = minValue;
        maxRange = maxValue;
    }

    public void sortData() {
        Collections.sort(data);
    }

    public void setLowerBound(Double lowerBound) {
        this.minRange = lowerBound;
    }

    public void setUpperBound(Double upperBound) {
        this.maxRange = upperBound;
    }

    public JPanel getPanel() {
        if (panel == null) {
            panel = new JQuickHistogramPanel(this);
        }
        return panel;
    }

    public void setConstraintHeight(int constraintHeight) {
        this.constraintHeight = constraintHeight;
        panel.setPreferredSize(new Dimension(constraintWidth, constraintHeight));
        panel.setMinimumSize(new Dimension(constraintWidth, constraintHeight));
    }

    public void setConstraintWidth(int constraintWidth) {
        this.constraintWidth = constraintWidth;
        panel.setPreferredSize(new Dimension(constraintWidth, constraintHeight));
        panel.setMinimumSize(new Dimension(constraintWidth, constraintHeight));
    }

    public int countValues() {
        return data.size();
    }

    public int countInRange() {
        int res = 0;
        for (int i = 0; i < data.size(); i++) {
            Double d = data.get(i);
            if ((inclusive && d >= minRange && d <= maxRange) || (!inclusive && d > minRange && d < maxRange)) {
                res++;
            }
        }
        return res;
    }

    public double getAverage() {
        double res = 0;
        for (int i = 0; i < data.size(); i++) {
            double d = data.get(i);
            res += d;
        }
        return res /= data.size();
    }

    public double getAverageInRange() {
        double res = 0;
        int c = 0;
        for (int i = 0; i < data.size(); i++) {
            double d = data.get(i);
            if ((inclusive && d >= minRange && d <= maxRange) || (!inclusive && d > minRange && d < maxRange)) {
                res += d;
                c++;
            }
        }
        return res /= c;
    }

    public double getMedian() {
        return data.get((data.size() + 1) / 2);
    }

    public double getMedianInRange() {
        int median = (countInRange() + 1) / 2;
        for (int i = 0; i < data.size(); i++) {
            double d = data.get(i);
            if ((inclusive && d >= minRange && d <= maxRange) || (!inclusive && d > minRange && d < maxRange)) {
                if (median-- == 0) {
                    return d;
                }
            }
        }
        return -1.;
    }

    private static class JQuickHistogramPanel extends JPanel {

        private final Color fillColor = new Color(0xCFD2D3);
        private final Color fillInRangeColor = new Color(0x3B4042);
        private final JQuickHistogram histogram;
        private int currentHeight = 0;
        private int currentWidth = 0;

        public JQuickHistogramPanel(JQuickHistogram histogram) {
            this.histogram = histogram;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            setCurrentDimension();
            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(0, currentHeight);
            g2d.scale(1, -1);
            drawHisto(g2d);
            g2d.dispose();
        }

        private void drawHisto(Graphics2D g2d) {

            if (histogram.minRange == null || histogram.maxRange == null) {
                return;
            }

            int dataSize = histogram.data.size();
            if (dataSize < currentWidth) {
                int rectWidth = (int) (currentWidth / (float) dataSize);
                int leftover = currentWidth - rectWidth * dataSize;
                int xPosition = 0;
                for (int i = 0; i < dataSize; i++) {
                    Double data = histogram.data.get(i);
                    int rectangleWidth = rectWidth + (leftover > 0 ? 1 : 0);
                    leftover--;
                    int rectangleHeight = (int) ((data - histogram.minValue) / (histogram.maxValue - histogram.minValue) * currentHeight);
                    if (data >= histogram.minRange && data <= histogram.maxRange) {
                        g2d.setColor(fillInRangeColor);
                    } else {
                        g2d.setColor(fillColor);
                    }
                    g2d.fillRect(xPosition, 0, rectangleWidth, rectangleHeight);

                    xPosition += rectangleWidth;
                }
            } else {
                int xPosition = 0;
                int sizeOfSmallSublists = dataSize / currentWidth;
                int sizeOfLargeSublists = sizeOfSmallSublists + 1;
                int numberOfLargeSublists = dataSize % currentWidth;
                int numberOfSmallSublists = currentWidth - numberOfLargeSublists;

                int numberOfElementsHandled = 0;
                for (int i = 0; i < currentWidth; i++) {
                    int size = i < numberOfSmallSublists ? sizeOfSmallSublists : sizeOfLargeSublists;
                    double average = 0.0;
                    for (int j = 0; j < size; j++) {
                        Double d = histogram.data.get(numberOfElementsHandled++);
                        average += d;
                    }
                    average /= size;
                    int rectangleHeight = (int) ((average - histogram.minValue) / (histogram.maxValue - histogram.minValue) * currentHeight);

                    if (average >= histogram.minRange && average <= histogram.maxRange) {
                        g2d.setColor(fillInRangeColor);
                    } else {
                        g2d.setColor(fillColor);
                    }
                    g2d.fillRect(xPosition, 0, 1, rectangleHeight);
                    xPosition++;
                }
            }
        }

        private void setCurrentDimension() {
            currentHeight = (histogram.constraintHeight > 0 ? histogram.constraintHeight : getHeight());
            currentWidth = (histogram.constraintWidth > 0 ? histogram.constraintWidth : getWidth());
        }
    }
}
