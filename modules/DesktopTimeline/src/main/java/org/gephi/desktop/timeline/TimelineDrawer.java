/*
Copyright 2008-2011 Gephi
Authors : Julian Bilcke
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

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import org.gephi.timeline.api.TimelineChart;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelEvent;
import org.openide.util.Lookup;

/**
 *
 * @author Julian Bilcke, Daniel Bernardes
 */
public class TimelineDrawer extends JPanel implements MouseListener, MouseMotionListener {

    //Consts
    private static final Cursor CURSOR_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
    private static final Cursor CURSOR_LEFT_HOOK = new Cursor(Cursor.E_RESIZE_CURSOR);
    private static final Cursor CURSOR_CENTRAL_HOOK = new Cursor(Cursor.MOVE_CURSOR);
    private static final Cursor CURSOR_RIGHT_HOOK = new Cursor(Cursor.W_RESIZE_CURSOR);
    private static final int LOC_RESIZE_FROM = 1;
    private static final int LOC_RESIZE_TO = 2;
    private static final int LOC_RESIZE_CENTER = 3;
    private static final int LOC_RESIZE_UNKNOWN = -1;
    //Settings
    private final DrawerSettings settings = new DrawerSettings();
    //Flags
    private Integer latestMousePositionX = null;
    private int currentMousePositionX = 0;
    private boolean mouseInside = false;
    //Model
    private TimelineModel model;
    private TimelineController controller;
    //Ticks
    private final TickGraph tickGraph = new TickGraph();
    //Sparkline
    private final Sparkline sparkline = new Sparkline();
    //Tooltip
    private final TimelineTooltip tooltip = new TimelineTooltip();

    public enum TimelineState {

        IDLE,
        MOVING,
        RESIZE_FROM,
        RESIZE_TO
    }
    TimelineState currentState = TimelineState.IDLE;

    public enum HighlightedComponent {

        NONE,
        LEFT_HOOK,
        RIGHT_HOOK,
        CENTER_HOOK
    }
    HighlightedComponent highlightedComponent = HighlightedComponent.NONE;

    public TimelineDrawer() {
        addMouseMotionListener(this);
        addMouseListener(this);
//        viewToModelSync = new Timer(150, updateModelAction);
//        viewToModelSync.setRepeats(true);
//        viewToModelSync.start();

        // setEnabled(true);
        // modelToViewSync = new Timer(2000, updateViewAction);
    }

    public void consumeEvent(TimelineModelEvent event) {
        switch (event.getEventType()) {
            case INTERVAL:
                double[] data = (double[]) event.getData();
                setInterval(data[0], data[1]);
                break;
            case CUSTOM_BOUNDS:
                double[] data2 = (double[]) event.getData();
                setCustomBounds(data2[0], data2[1]);
                break;
            case MIN_MAX:
                double[] data3 = (double[]) event.getData();
                setMinMax(data3[0], data3[1]);
                break;
            case CHART:
                setChart((TimelineChart) event.getData());
                break;
        }
    }

    public void setModel(TimelineModel model) {
        this.controller = Lookup.getDefault().lookup(TimelineController.class);
        this.model = model;
        if (model != null) {
            setMinMax(model.getMin(), model.getMax());
            if (model.hasCustomBounds()) {
                setCustomBounds(model.getCustomMin(), model.getCustomMax());
            }
            setInterval(model.getIntervalStart(), model.getIntervalEnd());
        } else {
            repaint();
        }
    }

    public void setChart(TimelineChart chart) {
        repaint();
    }

    public void setMinMax(double min, double max) {
        repaint();
    }

    public void setCustomBounds(double min, double max) {
        repaint();
    }

    public void setInterval(double from, double to) {
        repaint();
    }

    public int getPixelPosition(double val, double duration, double min, int width) {
        return (int) ((val - min) * (width / duration));
    }

    public double getReal(int pixel, double duration, double min, int width) {
        return pixel * (duration / width) + min;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setMinimumSize(new java.awt.Dimension(300, 28));
        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        settings.update(width, height);
        Graphics2D g2d = (Graphics2D) g;

        int innerWidth = width - 1;
        int innerHeight = height - settings.tmMarginBottom - 2;
        int innerY = settings.tmMarginTop + 1;
        if (settings.background.top != null) {
            g2d.setColor(settings.background.top);
            g2d.fillRect(0, innerY, innerWidth, innerHeight);
        }
//        g2d.setBackground(settings.background.top);
//        g2d.setPaint(settings.background.paint);
//        g2d.setColor(settings.background.top);
//        g2d.fillRect(0, innerY, innerWidth, innerHeight);

        if (!this.isEnabled()) {
            return;
        }
        if (model == null) {
            return;
        }

        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int intervalStartPixel = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int intervalEndPixel = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

        g2d.setRenderingHints(settings.renderingHints);

        //TICKS
        g2d.drawImage(tickGraph.getImage(model, innerWidth, innerHeight), 0, innerY, null);

        //SPARKLINE
        if (model.getChart() != null) {
            BufferedImage spImage = sparkline.getImage(model, innerWidth, innerHeight - settings.topChartMargin);
            g2d.drawImage(spImage, 0, innerY + settings.topChartMargin, null);
        }

        // VISIBLE HOOK (THE LITTLE GREEN RECTANGLE ON EACH SIDE) WIDTH
        int vhw = settings.selection.visibleHookWidth;

        // SELECTED ZONE WIDTH, IN PIXELS
        int sw = intervalEndPixel - intervalStartPixel;

        if (highlightedComponent != HighlightedComponent.NONE) {
            g2d.setPaint(settings.selection.mouseOverPaint);
            switch (highlightedComponent) {
                case LEFT_HOOK:
                    g2d.fillRect(
                            intervalStartPixel,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalStartPixel + vhw,
                            settings.tmMarginTop,
                            sw - vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
                case CENTER_HOOK:
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalStartPixel,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.mouseOverPaint);
                    g2d.fillRect(
                            intervalStartPixel + vhw,
                            settings.tmMarginTop,
                            sw - vhw * 2,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalEndPixel - vhw,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
                case RIGHT_HOOK:
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            intervalStartPixel,
                            settings.tmMarginTop,
                            sw - vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.mouseOverPaint);
                    g2d.fillRect(
                            intervalEndPixel - vhw,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
            }
        } else {
            g2d.setPaint(settings.selection.paint);
            g2d.fillRect(intervalStartPixel, settings.tmMarginTop, sw, height - settings.tmMarginBottom - 1);
        }

        g2d.setColor(settings.defaultStrokeColor);
        g2d.drawRect(intervalStartPixel, settings.tmMarginTop, sw - 1, height - settings.tmMarginBottom - 1);

        double v = getReal(currentMousePositionX, max - min, min, width);
    }

    private boolean inRange(int x, int a, int b) {
        return (a < x && x < b);
    }

    /**
     * Position of current x.
     *
     * @param x current location
     * @param r width of slider
     * @return LOC_RESIZE_*
     */
    private int inPosition(int x, int r, int sf, int st) {
        boolean resizeFrom = inRange(x, (int) sf - 1, (int) sf + r + 1);
        boolean resizeTo = inRange(x, (int) st - r - 1, (int) st + 1);
        if (resizeFrom && resizeTo) {
            if (inRange(x, (int) sf - 1, (int) (sf + st) / 2)) {
                return LOC_RESIZE_FROM;
            } else if (inRange(x, (int) (sf + st) / 2, (int) st + 1)) {
                return LOC_RESIZE_TO;
            }
        }
        if (resizeFrom) {
            return LOC_RESIZE_FROM;
        } else if (inRange(x, (int) sf + r, (int) st - r)) {
            return LOC_RESIZE_CENTER;
        } else if (resizeTo) {
            return LOC_RESIZE_TO;
        } else {
            return LOC_RESIZE_UNKNOWN;
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        latestMousePositionX = e.getX();
        currentMousePositionX = latestMousePositionX;

        //On double click in a left/right handle, set the interval to the min/max
        if (e.getClickCount() == 2) {
            int x = e.getX();
            int r = settings.selection.visibleHookWidth + settings.selection.invisibleHookMargin;

            int width = getWidth();
            double min = model.getCustomMin();
            double max = model.getCustomMax();
            double intervalStart = model.getIntervalStart();
            double intervalEnd = model.getIntervalEnd();

            int sf = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
            int st = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

            int position = inPosition(x, r, sf, st);
            switch (position) {
                case LOC_RESIZE_FROM:
                    controller.setInterval(min, intervalEnd);
                    break;
                case LOC_RESIZE_CENTER:
                    controller.setInterval(min, max);
                    break;
                case LOC_RESIZE_TO:
                    controller.setInterval(intervalStart, max);
                    break;
            }

            setInterval(model.getIntervalStart(), model.getIntervalEnd());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (model == null) {
            return;
        }
        int x = e.getX();
        latestMousePositionX = x;
        currentMousePositionX = latestMousePositionX;
        int r = settings.selection.visibleHookWidth + settings.selection.invisibleHookMargin;

        tooltip.stop();

        int width = getWidth();
        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int sf = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int st = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

        if (currentState == TimelineState.IDLE) {
            int position = inPosition(x, r, sf, st);
            switch (position) {
                case LOC_RESIZE_FROM:
                    highlightedComponent = HighlightedComponent.LEFT_HOOK;
                    currentState = TimelineState.RESIZE_FROM;
                    break;
                case LOC_RESIZE_CENTER:
                    highlightedComponent = HighlightedComponent.CENTER_HOOK;
                    currentState = TimelineState.MOVING;
                    break;
                case LOC_RESIZE_TO:
                    highlightedComponent = HighlightedComponent.RIGHT_HOOK;
                    currentState = TimelineState.RESIZE_TO;
                    break;
                default:
                    break;
            }
        }
//        if(e.isPopupTrigger()) {
//            System.out.println("popup!");
//            MetricPopup.setLocation(e.getX(), e.getY());
//            MetricPopup.setVisible(true);
//        }        
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (currentState == TimelineState.IDLE) {
            latestMousePositionX = e.getX();
            currentMousePositionX = latestMousePositionX;
        }
        mouseInside = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (currentState == TimelineState.IDLE) {
            highlightedComponent = HighlightedComponent.NONE;
            latestMousePositionX = e.getX();
            currentMousePositionX = latestMousePositionX;
        }
        mouseInside = false;
        tooltip.stop();
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent evt) {

        latestMousePositionX = evt.getX();
        currentMousePositionX = latestMousePositionX;
        //highlightedComponent = HighlightedComponent.NONE;
        currentState = TimelineState.IDLE;
        this.getParent().repaint(); // so it will repaint upper and bottom panes
    }

    @Override
    public void mouseMoved(MouseEvent evt) {
        if (model == null) {
            return;
        }

        //System.out.println("mouse moved");
        currentMousePositionX = evt.getX();
        int x = currentMousePositionX;
        int width = getWidth();
        int r = settings.selection.visibleHookWidth;

        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int sf = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int st = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

        //Tooltip
        double pos = getReal(currentMousePositionX, max - min, min, width);
        tooltip.setModel(model);
        tooltip.start(pos, evt.getLocationOnScreen(), this);

        // SELECTED ZONE BEGIN POSITION, IN PIXELS
        // int sf = (int) (model.getFromFloat() * (double) w);
        // SELECTED ZONE END POSITION, IN PIXELS
        //int st = (int) (model.getToFloat() * (double) w);
        HighlightedComponent old = highlightedComponent;
        Cursor newCursor;

        int a = 0;//settings.selection.invisibleHookMargin;

        int position = inPosition(x, r, sf, st);
        switch (position) {
            case LOC_RESIZE_FROM:
                newCursor = CURSOR_LEFT_HOOK;
                highlightedComponent = HighlightedComponent.LEFT_HOOK;
                break;
            case LOC_RESIZE_CENTER:
                highlightedComponent = HighlightedComponent.CENTER_HOOK;
                newCursor = CURSOR_CENTRAL_HOOK;
                break;
            case LOC_RESIZE_TO:
                highlightedComponent = HighlightedComponent.RIGHT_HOOK;
                newCursor = CURSOR_RIGHT_HOOK;
                break;
            default:
                highlightedComponent = HighlightedComponent.NONE;
                newCursor = CURSOR_DEFAULT;
                break;
        }
        if (newCursor != getCursor()) {
            setCursor(newCursor);
        }
        // only repaint if highlight has changed (save a lot of fps)
        if (highlightedComponent != old) {
            repaint();
        }
//         now we always repaint, because of the tooltip
//        repaint();

    }

    @Override
    public void mouseDragged(MouseEvent evt) {
        if (model == null) {
            return;
        }
        int width = getWidth();

        double min = model.getCustomMin();
        double max = model.getCustomMax();
        double intervalStart = model.getIntervalStart();
        double intervalEnd = model.getIntervalEnd();

        int sf = Math.max(0, getPixelPosition(intervalStart, max - min, min, width));
        int st = Math.min(width, getPixelPosition(intervalEnd, max - min, min, width));

        currentMousePositionX = evt.getX();
        currentMousePositionX = Math.max(0, currentMousePositionX);
        currentMousePositionX = Math.min(width, currentMousePositionX);
        int x = currentMousePositionX;

        tooltip.stop();

        int r = settings.selection.visibleHookWidth;

        // SELECTED ZONE BEGIN POSITION, IN PIXELS
        // sf = (model.getFromFloat() * w);
        // SELECTED ZONE END POSITION, IN PIXELS
        //st = (model.getToFloat() * w);
        if (currentState == TimelineState.IDLE) {
            int position = inPosition(x, r, sf, st);
            switch (position) {
                case LOC_RESIZE_FROM:
                    highlightedComponent = HighlightedComponent.LEFT_HOOK;
                    currentState = TimelineState.RESIZE_FROM;
                    break;
                case LOC_RESIZE_CENTER:
                    highlightedComponent = HighlightedComponent.CENTER_HOOK;
                    currentState = TimelineState.MOVING;
                    break;
                case LOC_RESIZE_TO:
                    highlightedComponent = HighlightedComponent.RIGHT_HOOK;
                    currentState = TimelineState.RESIZE_TO;
                    break;
                default:
                    break;
            }
        }
        int deltaPx = 0;
        if (latestMousePositionX != null) {
            deltaPx = x - latestMousePositionX;
        }
        latestMousePositionX = x;

        double deltaTimestamp = getReal(deltaPx, max - min, 0, width);
        double newFrom = intervalStart;
        double newTo = intervalEnd;

        switch (currentState) {
            case RESIZE_FROM:
                //problem: moving the left part will crush the security zone
                newFrom += deltaTimestamp;
                break;
            case RESIZE_TO:
                newTo += deltaTimestamp;
                break;
            case MOVING:
                if ((sf + deltaPx) < 0) {
                    newTo = (newTo - newFrom);
                    newFrom = min;
                    // .. or the right
                } else if ((st + deltaPx) >= width) {
                    newFrom = max - (newTo - newFrom);
                    newTo = max;
                } else {
                    newFrom += deltaTimestamp;
                    newTo += deltaTimestamp;
                }
                break;
        }

        // minimal selection zone width (a security to not crush it!)
        int s = settings.selection.minimalWidth;

        int sfNew = Math.max(0, getPixelPosition(newFrom, max - min, min, width));
        int stNew = Math.min(width, getPixelPosition(newTo, max - min, min, width));
        if (width != 0 && stNew - sfNew >= s) {
            newFrom = Math.max(newFrom, model.getCustomMin());
            newTo = Math.min(newTo, model.getCustomMax());
            if (newFrom < newTo) {
                controller.setInterval(newFrom, newTo);
            }
        }
    }
}
