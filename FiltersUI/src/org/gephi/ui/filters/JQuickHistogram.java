/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.filters;

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
        data = new ArrayList<Double>();
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

    private static class JQuickHistogramPanel extends JPanel {

        private Color fillColor = new Color(0xCFD2D3);
        private Color fillInRangeColor = new Color(0x3B4042);
        private JQuickHistogram histogram;
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

            int dataSize = histogram.data.size();
            if (dataSize < currentWidth) {
                int rectWidth = (int) (currentWidth / (float) dataSize);
                int leftover = currentWidth - rectWidth * dataSize;
                System.out.println(rectWidth + " " + leftover);
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
                        average += d.doubleValue();
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
