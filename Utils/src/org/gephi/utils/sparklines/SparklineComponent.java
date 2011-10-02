/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.utils.sparklines;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * <p>Simple component that holds a <code>SparklineGraph</code> and auto-repaints it when mouse interaction happens if desired
 * (indicate it with <code>updateMouseXPosition</code> parameter in constructors).</p>
 * <p>It also takes care to update sparkline width and height to the component width and height when resized</p>
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SparklineComponent extends JComponent {

    private Number[] xValues, yValues;
    private Number yMinValue, yMaxValue;
    private SparklineParameters sparklineParameters;

    public SparklineComponent(Number[] yValues, SparklineParameters sparklineParameters, boolean updateMouseXPosition) {
        this(null, yValues, null, null, sparklineParameters, updateMouseXPosition);
    }

    public SparklineComponent(Number[] yValues, Number yMinValue, Number yMaxValue, SparklineParameters sparklineParameters, boolean updateMouseXPosition) {
        this(null, yValues, yMinValue, yMaxValue, sparklineParameters, updateMouseXPosition);
    }

    public SparklineComponent(Number[] xValues, Number[] yValues, SparklineParameters sparklineParameters, boolean updateMouseXPosition) {
        this(xValues, yValues, null, null, sparklineParameters, updateMouseXPosition);
    }

    public SparklineComponent(Number[] xValues, Number[] yValues, Number yMinValue, Number yMaxValue, SparklineParameters sparklineParameters, boolean updateMouseXPosition) {
        this.xValues = xValues;
        this.yValues = yValues;
        this.yMinValue = yMinValue;
        this.yMaxValue = yMaxValue;
        this.sparklineParameters = sparklineParameters;
        if (updateMouseXPosition) {
            initEvents();
        }
    }

    private void initEvents() {
        MouseEvents listener = new MouseEvents();
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    @Override
    protected void paintComponent(Graphics g) {
        sparklineParameters.setWidth(getWidth());
        sparklineParameters.setHeight(getHeight());
        BufferedImage image = SparklineGraph.draw(xValues, yValues, yMinValue, yMaxValue, sparklineParameters);
        g.drawImage(image, 0, 0, this);
    }

    class MouseEvents extends MouseAdapter implements MouseMotionListener {

        @Override
        public void mouseEntered(MouseEvent e) {
            sparklineParameters.setHiglightedValueXPosition(e.getX());
            repaint();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            sparklineParameters.setHiglightedValueXPosition(null);
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            sparklineParameters.setHiglightedValueXPosition(e.getX());
            repaint();
        }
    }
}
