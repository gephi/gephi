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
package org.gephi.ui.components.splineeditor.equation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.GeneralPath;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;

public class EquationDisplay extends JComponent implements PropertyChangeListener {

    private static final Color COLOR_BACKGROUND = Color.WHITE;
    private static final Color COLOR_MAJOR_GRID = Color.GRAY.brighter();
    private static final Color COLOR_MINOR_GRID = new Color(220, 220, 220);
    private static final Color COLOR_AXIS = Color.BLACK;
    private static final float STROKE_AXIS = 1.2f;
    private static final float STROKE_GRID = 1.0f;
    private static final float COEFF_ZOOM = 1.1f;
    private List<DrawableEquation> equations;
    protected double minX;
    protected double maxX;
    protected double minY;
    protected double maxY;
    private double originX;
    private double originY;
    private double majorX;
    private int minorX;
    private double majorY;
    private int minorY;
    private boolean drawText = true;
    private Point dragStart;
    private NumberFormat formatter;
    private ZoomHandler zoomHandler;
    private PanMotionHandler panMotionHandler;
    private PanHandler panHandler;

    public EquationDisplay(double originX, double originY,
            double minX, double maxX,
            double minY, double maxY,
            double majorX, int minorX,
            double majorY, int minorY) {
        if (minX >= maxX) {
            throw new IllegalArgumentException("minX must be < to maxX");
        }

        if (originX < minX || originX > maxX) {
            throw new IllegalArgumentException("originX must be between minX and maxX");
        }

        if (minY >= maxY) {
            throw new IllegalArgumentException("minY must be < to maxY");
        }

        if (originY < minY || originY > maxY) {
            throw new IllegalArgumentException("originY must be between minY and maxY");
        }

        if (minorX <= 0) {
            throw new IllegalArgumentException("minorX must be > 0");
        }

        if (minorY <= 0) {
            throw new IllegalArgumentException("minorY must be > 0");
        }

        if (majorX <= 0.0) {
            throw new IllegalArgumentException("majorX must be > 0.0");
        }

        if (majorY <= 0.0) {
            throw new IllegalArgumentException("majorY must be > 0.0");
        }

        this.originX = originX;
        this.originY = originY;

        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;

        this.majorX = majorX;
        this.minorX = minorX;
        this.majorY = majorY;
        this.minorY = minorY;

        this.equations = new LinkedList<DrawableEquation>();

        this.formatter = NumberFormat.getInstance();
        this.formatter.setMaximumFractionDigits(2);

        panHandler = new PanHandler();
        addMouseListener(panHandler);
        panMotionHandler = new PanMotionHandler();
        addMouseMotionListener(panMotionHandler);
        zoomHandler = new ZoomHandler();
        addMouseWheelListener(zoomHandler);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() != enabled) {
            //super.setEnabled(enabled);

            if (enabled) {
                addMouseListener(panHandler);
                addMouseMotionListener(panMotionHandler);
                addMouseWheelListener(zoomHandler);
            } else {
                removeMouseListener(panHandler);
                removeMouseMotionListener(panMotionHandler);
                removeMouseWheelListener(zoomHandler);
            }
        }
    }

    public boolean isDrawText() {
        return drawText;
    }

    public void setDrawText(boolean drawText) {
        this.drawText = drawText;
    }

    public void addEquation(AbstractEquation equation, Color color) {
        if (equation != null && !equations.contains(equation)) {
            equation.addPropertyChangeListener(this);
            equations.add(new DrawableEquation(equation, color));
            repaint();
        }
    }

    public void removeEquation(AbstractEquation equation) {
        if (equation != null) {
            DrawableEquation toRemove = null;
            for (DrawableEquation drawable : equations) {
                if (drawable.getEquation() == equation) {
                    toRemove = drawable;
                    break;
                }
            }

            if (toRemove != null) {
                equation.removePropertyChangeListener(this);
                equations.remove(toRemove);
                repaint();
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(400, 400);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        repaint();
    }

    protected double yPositionToPixel(double position) {
        double height = getHeight();
        return height - ((position - minY) * height / (maxY - minY));
    }

    protected double xPositionToPixel(double position) {
        return (position - minX) * getWidth() / (maxX - minX);
    }

    protected double xPixelToPosition(double pixel) {
        double axisV = xPositionToPixel(originX);
        return (pixel - axisV) * (maxX - minX) / getWidth();
    }

    protected double yPixelToPosition(double pixel) {
        double axisH = yPositionToPixel(originY);
        return (getHeight() - pixel - axisH) * (maxY - minY) / getHeight();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (!isVisible()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        setupGraphics(g2);

        paintBackground(g2);
        drawGrid(g2);
        drawAxis(g2);

        drawEquations(g2);

        paintInformation(g2);
    }

    protected void paintInformation(Graphics2D g2) {
    }

    private void drawEquations(Graphics2D g2) {
        for (DrawableEquation drawable : equations) {
            g2.setColor(drawable.getColor());
            drawEquation(g2, drawable.getEquation());
        }
    }

    private void drawEquation(Graphics2D g2, AbstractEquation equation) {
        float x = 0.0f;
        float y = (float) yPositionToPixel(equation.compute(xPixelToPosition(0.0)));

        GeneralPath path = new GeneralPath();
        path.moveTo(x, y);

        for (x = 0.0f; x < getWidth(); x += 1.0f) {
            double position = xPixelToPosition(x);
            y = (float) yPositionToPixel(equation.compute(position));
            path.lineTo(x, y);
        }

        g2.draw(path);
    }

    private void drawGrid(Graphics2D g2) {
        Stroke stroke = g2.getStroke();

        drawVerticalGrid(g2);
        drawHorizontalGrid(g2);

        if (drawText) {
            drawVerticalLabels(g2);
            drawHorizontalLabels(g2);
        }

        g2.setStroke(stroke);
    }

    private void drawHorizontalLabels(Graphics2D g2) {
        double axisV = xPositionToPixel(originX);

        g2.setColor(COLOR_AXIS);
        for (double y = originY + majorY; y < maxY + majorY; y += majorY) {
            int position = (int) yPositionToPixel(y);
            g2.drawString(formatter.format(y), (int) axisV + 5, position);
        }

        for (double y = originY - majorY; y > minY - majorY; y -= majorY) {
            int position = (int) yPositionToPixel(y);
            g2.drawString(formatter.format(y), (int) axisV + 5, position);
        }
    }

    private void drawHorizontalGrid(Graphics2D g2) {
        double minorSpacing = majorY / minorY;
        double axisV = xPositionToPixel(originX);

        Stroke gridStroke = new BasicStroke(STROKE_GRID);
        Stroke axisStroke = new BasicStroke(STROKE_AXIS);

        for (double y = originY + majorY; y < maxY + majorY; y += majorY) {
            g2.setStroke(gridStroke);
            g2.setColor(COLOR_MINOR_GRID);
            for (int i = 0; i < minorY; i++) {
                int position = (int) yPositionToPixel(y - i * minorSpacing);
                g2.drawLine(0, position, getWidth(), position);
            }

            int position = (int) yPositionToPixel(y);
            g2.setColor(COLOR_MAJOR_GRID);
            g2.drawLine(0, position, getWidth(), position);

            g2.setStroke(axisStroke);
            g2.setColor(COLOR_AXIS);
            g2.drawLine((int) axisV - 3, position, (int) axisV + 3, position);
        }

        for (double y = originY - majorY; y > minY - majorY; y -= majorY) {
            g2.setStroke(gridStroke);
            g2.setColor(COLOR_MINOR_GRID);
            for (int i = 0; i < minorY; i++) {
                int position = (int) yPositionToPixel(y + i * minorSpacing);
                g2.drawLine(0, position, getWidth(), position);
            }

            int position = (int) yPositionToPixel(y);
            g2.setColor(COLOR_MAJOR_GRID);
            g2.drawLine(0, position, getWidth(), position);

            g2.setStroke(axisStroke);
            g2.setColor(COLOR_AXIS);
            g2.drawLine((int) axisV - 3, position, (int) axisV + 3, position);
        }
    }

    private void drawVerticalLabels(Graphics2D g2) {
        double axisH = yPositionToPixel(originY);
        FontMetrics metrics = g2.getFontMetrics();

        g2.setColor(COLOR_AXIS);

        for (double x = originX + majorX; x < maxX + majorX; x += majorX) {
            int position = (int) xPositionToPixel(x);
            g2.drawString(formatter.format(x), position, (int) axisH + metrics.getHeight());
        }

        for (double x = originX - majorX; x > minX - majorX; x -= majorX) {
            int position = (int) xPositionToPixel(x);
            g2.drawString(formatter.format(x), position, (int) axisH + metrics.getHeight());
        }
    }

    private void drawVerticalGrid(Graphics2D g2) {
        double minorSpacing = majorX / minorX;
        double axisH = yPositionToPixel(originY);

        Stroke gridStroke = new BasicStroke(STROKE_GRID);
        Stroke axisStroke = new BasicStroke(STROKE_AXIS);

        for (double x = originX + majorX; x < maxX + majorX; x += majorX) {
            g2.setStroke(gridStroke);
            g2.setColor(COLOR_MINOR_GRID);
            for (int i = 0; i < minorX; i++) {
                int position = (int) xPositionToPixel(x - i * minorSpacing);
                g2.drawLine(position, 0, position, getHeight());
            }

            int position = (int) xPositionToPixel(x);
            g2.setColor(COLOR_MAJOR_GRID);
            g2.drawLine(position, 0, position, getHeight());

            g2.setStroke(axisStroke);
            g2.setColor(COLOR_AXIS);
            g2.drawLine(position, (int) axisH - 3, position, (int) axisH + 3);
        }

        for (double x = originX - majorX; x > minX - majorX; x -= majorX) {
            g2.setStroke(gridStroke);
            g2.setColor(COLOR_MINOR_GRID);
            for (int i = 0; i < minorX; i++) {
                int position = (int) xPositionToPixel(x + i * minorSpacing);
                g2.drawLine(position, 0, position, getHeight());
            }

            int position = (int) xPositionToPixel(x);
            g2.setColor(COLOR_MAJOR_GRID);
            g2.drawLine(position, 0, position, getHeight());

            g2.setStroke(axisStroke);
            g2.setColor(COLOR_AXIS);
            g2.drawLine(position, (int) axisH - 3, position, (int) axisH + 3);
        }
    }

    private void drawAxis(Graphics2D g2) {
        double axisH = yPositionToPixel(originY);
        double axisV = xPositionToPixel(originX);

        g2.setColor(COLOR_AXIS);
        Stroke stroke = g2.getStroke();
        g2.setStroke(new BasicStroke(STROKE_AXIS));

        g2.drawLine(0, (int) axisH, getWidth(), (int) axisH);
        g2.drawLine((int) axisV, 0, (int) axisV, getHeight());

        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(formatter.format(0.0), (int) axisV + 5, (int) axisH + metrics.getHeight());

        g2.setStroke(stroke);
    }

    protected void setupGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }

    protected void paintBackground(Graphics2D g2) {
        g2.setColor(COLOR_BACKGROUND);
        g2.fill(g2.getClipBounds());
    }

    private class DrawableEquation {

        private AbstractEquation equation;
        private Color color;

        DrawableEquation(AbstractEquation equation, Color color) {
            this.equation = equation;
            this.color = color;
        }

        AbstractEquation getEquation() {
            return equation;
        }

        Color getColor() {
            return color;
        }
    }

    private class ZoomHandler implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            double distanceX = maxX - minX;
            double distanceY = maxY - minY;

            double cursorX = minX + distanceX / 2.0;
            double cursorY = minY + distanceY / 2.0;

            int rotation = e.getWheelRotation();
            if (rotation < 0) {
                distanceX /= COEFF_ZOOM;
                distanceY /= COEFF_ZOOM;
            } else {
                distanceX *= COEFF_ZOOM;
                distanceY *= COEFF_ZOOM;
            }

            minX = cursorX - distanceX / 2.0;
            maxX = cursorX + distanceX / 2.0;
            minY = cursorY - distanceY / 2.0;
            maxY = cursorY + distanceY / 2.0;

            repaint();
        }
    }

    private class PanHandler extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
        }
    }

    private class PanMotionHandler extends MouseMotionAdapter {

        @Override
        public void mouseDragged(MouseEvent e) {
            Point dragEnd = e.getPoint();

            double distance = xPixelToPosition(dragEnd.getX())
                    - xPixelToPosition(dragStart.getX());
            minX -= distance;
            maxX -= distance;

            distance = yPixelToPosition(dragEnd.getY())
                    - yPixelToPosition(dragStart.getY());
            minY -= distance;
            maxY -= distance;

            repaint();
            dragStart = dragEnd;
        }
    }

    public List<DrawableEquation> getEquations() {
        return equations;
    }
}
