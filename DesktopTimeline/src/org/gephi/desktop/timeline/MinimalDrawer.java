/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import org.gephi.timeline.api.TimelineAnimator;
import org.gephi.timeline.api.TimelineAnimatorListener;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.timeline.api.TimelineModelListener;
import org.gephi.timeline.spi.TimelineDrawer;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jbilcke
 */
@ServiceProvider(service = TimelineDrawer.class)
public class MinimalDrawer extends JPanel
        implements TimelineDrawer,
        MouseListener,
        MouseMotionListener,
        TimelineAnimatorListener {

    private static final long serialVersionUID = 1L;
    private MinimalDrawerSettings settings = new MinimalDrawerSettings();
    private TimelineModel model = null;
    private TimelineAnimator animator = null;
    private Integer latestMousePositionX = null;
    private int currentMousePositionX = 0;
    private static Cursor CURSOR_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
    private static Cursor CURSOR_LEFT_HOOK = new Cursor(Cursor.E_RESIZE_CURSOR);
    private static Cursor CURSOR_CENTRAL_HOOK = new Cursor(Cursor.MOVE_CURSOR);
    private static Cursor CURSOR_RIGHT_HOOK = new Cursor(Cursor.W_RESIZE_CURSOR);
    private static final int LOC_RESIZE_FROM = 1;
    private static final int LOC_RESIZE_TO = 2;
    private static final int LOC_RESIZE_CENTER = 3;
    private static final int LOC_RESIZE_UNKNOWN = -1;
    private static Locale LOCALE = Locale.ENGLISH;
    private double newfrom = 0;
    private double newto = 1;
    private double sf = -1;
    private double st = -1;
    private Timer viewToModelSync = null;
    private Timer modelToViewSync = null;
    private double oldfrom = -1;
    private double oldto = -1;
    public Action updateModelAction = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            if (model != null) {
                if (newfrom != oldfrom || newto != oldto) {
                    model.setRangeFromFloat(newfrom, newto);
                    oldfrom = newfrom;
                    oldto = newto;
                }
            }
        }
    };
    public Action updateViewAction = new AbstractAction() {

        public void actionPerformed(ActionEvent e) {
            if (model != null) {
                double tsf = model.getFromFloat() * (double) getWidth();
                double tst = model.getToFloat() * (double) getWidth();
                if (tsf != sf) {
                    sf = tsf;
                }
                if (tst != st) {
                    st = tst;
                }
            }
        }
    };
    private boolean mouseInside = false;

    public enum TimelineLevel {

        MILLISECOND,
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR,
        YEAR_10,
        YEAR_20,
        YEAR_50,
        YEAR_100
    }

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

    /** Creates new form MinimalDrawer */
    public MinimalDrawer() {

        //System.out.println("width: " + getWidth());
        //System.out.println("height: " + getHeight());
        setVisible(true);
        addMouseMotionListener(this);
        addMouseListener(this);
        // setEnabled(true);
        viewToModelSync = new Timer(150, updateModelAction);
        viewToModelSync.setRepeats(true);
        viewToModelSync.start();

        // setEnabled(true);
        // modelToViewSync = new Timer(2000, updateViewAction);

    }

    public void setModel(TimelineModel model) {
        if (model == null) {
            return;
        }
        if (model != this.model) {
            if (this.model != null) {
            }
            this.model = model;
            sf = model.getFromFloat() * (double) getWidth();
            st = model.getToFloat() * (double) getWidth();
            repaint();
        }
    }

    public TimelineModel getModel() {
        return model;
    }

    public void setAnimator(TimelineAnimator animator) {
        this.animator = animator;
    }

    public TimelineAnimator getAnimator() {
        return animator;
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
        // initialization
        if (st == 0) {
            if (model != null) {
                sf = 0.0 * (double) width;
                st = 1.0 * (double) width;
                newfrom = sf * (1.0 / width);
                newto = st * (1.0 / width);
            }
        }



        settings.update(width, height);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setBackground(settings.background.top);
        g2d.setPaint(settings.background.paint);
        g2d.fillRect(0, settings.tmMarginTop + 1, width, height - settings.tmMarginBottom - 2);

        if (!this.isEnabled()) {
            return;
        }
        if (model == null) {
            return;
        }

        long min = (long) model.getMinValue();
        long max = (long) model.getMaxValue();
        /*
        System.out.println("\nall min: " + min);
        System.out.println("all max: " + max);
        System.out.println("all date min: " + new DateTime(new Date(min)));
        System.out.println("all date max: " + new DateTime(new Date(max)));
         */

        if (max <= min
                || min == Double.NEGATIVE_INFINITY
                || max == Double.POSITIVE_INFINITY
                || max == Double.NEGATIVE_INFINITY
                || min == Double.POSITIVE_INFINITY) {
            System.out.println("cannot show a model with negative values");
            return;
        }
        if (model.getFromFloat() == Double.NEGATIVE_INFINITY
                || model.getToFloat() == Double.POSITIVE_INFINITY) {
            System.out.println("cannot show a selection with negative values");
            return;
        }

        /*
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("date min: " + new DateTime(new Date(min)));
        System.out.println("date max: " + new DateTime(new Date(max)));
         */

        g2d.setRenderingHints(settings.renderingHints);


        // VISIBLE HOOK (THE LITTLE GREEN RECTANGLE ON EACH SIDE) WIDTH
        int vhw = settings.selection.visibleHookWidth;

        // SELECTED ZONE WIDTH, IN PIXELS
        int sw = (int) st - (int) sf;

        if (highlightedComponent != HighlightedComponent.NONE) {
            g2d.setPaint(settings.selection.mouseOverPaint);
            switch (highlightedComponent) {
                case LEFT_HOOK:
                    g2d.fillRect(
                            (int) sf,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            (int) sf + vhw,
                            settings.tmMarginTop,
                            sw - vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
                case CENTER_HOOK:
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            (int) sf,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.mouseOverPaint);
                    g2d.fillRect(
                            (int) sf + vhw,
                            settings.tmMarginTop,
                            sw - vhw * 2,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            (int) st - vhw,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
                case RIGHT_HOOK:
                    g2d.setPaint(settings.selection.paint);
                    g2d.fillRect(
                            (int) sf,
                            settings.tmMarginTop,
                            sw - vhw,
                            height - settings.tmMarginBottom - 1);
                    g2d.setPaint(settings.selection.mouseOverPaint);
                    g2d.fillRect(
                            (int) st - vhw,
                            settings.tmMarginTop,
                            vhw,
                            height - settings.tmMarginBottom - 1);
                    break;
            }
        } else {
            g2d.setPaint(settings.selection.paint);
            g2d.fillRect((int) sf, settings.tmMarginTop, sw, height - settings.tmMarginBottom - 1);
        }

        //DateTime dtFrom = new DateTime(1455, 1, 1, 1, 1, 1, 1);
        //DateTime dtTo = new DateTime(1960, 2, 10, 1, 1, 1, 1);
        if (model.getUnit() == DateTime.class) {
            paintUpperRulerForInterval(g2d,
                    new DateTime(new Date(min)),
                    new DateTime(new Date(max)));
        }

        g2d.setColor(settings.defaultStrokeColor);
        g2d.drawRect((int) sf, settings.tmMarginTop, sw, height - settings.tmMarginBottom - 1);

        double v = model.getValueFromFloat(currentMousePositionX * (1.0 / width));

        v += model.getMinValue();

        if (v != Double.NEGATIVE_INFINITY && v != Double.POSITIVE_INFINITY) {
            String str = "";
            int strw = 0;
            if (model.getUnit() == DateTime.class) {
                DateTime d = new DateTime(new Date((long) v));
                if (d != null) {

                    DateTimeFormatter fmt = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
                    str = fmt.withLocale(LOCALE).print(d);
                    strw = (int) (settings.tip.fontMetrics.getStringBounds(str, null)).getWidth() + 4;
                }
            } else {
                str = new Double(v).toString();
                strw = (int) (settings.tip.fontMetrics.getStringBounds(str, null)).getWidth() + 4;
            }

            int px = currentMousePositionX;
            if (px + strw >= width) {
                px = width - strw;
            }

            if (mouseInside) {
                g2d.setPaint(settings.tip.backgroundColor);
                g2d.fillRect(px, 1, strw, 18);
                g2d.setPaint(settings.tip.fontColor);
                g2d.drawRect(px, 1, strw, 18);
                g2d.setColor(settings.tip.fontColor);
                g2d.drawString(str, px + 4, 16);
            }

        }
    }

    private void paintUpperRulerForInterval(Graphics2D g2d, DateTime dtFrom, DateTime dtTo) {

        g2d.setFont(settings.graduations.font);
        g2d.setColor(settings.graduations.fontColor);
        int leftMargin = settings.graduations.leftMargin;
        int textTopPosition = settings.graduations.textTopPosition;
        int width = getWidth();
        int height = getHeight();
        // TODO take these from the model


        Interval interval = new Interval(dtFrom, dtTo);

        Period p = interval.toPeriod(PeriodType.days());
        // try to determine length if we had to show milliseconds

        int n = p.getDays();
        int unitSize = (int) (settings.graduations.fontMetrics.getStringBounds("wednesday  ", null)).getWidth();
        if (n < (width / unitSize)) {
            //System.out.println("jour");
            for (int i = 0; i < n; i++) {
                g2d.drawString(dtFrom.plusDays(i).dayOfWeek().getAsText(LOCALE),
                        leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Hours.hoursBetween(dtFrom.plusDays(i), dtFrom.plusDays(i + 1)).getHours());
            }
            return;
        }


        unitSize = (int) (settings.graduations.fontMetrics.getStringBounds("wed ", null)).getWidth();
        if (n < (width / unitSize)) {
            //System.out.println("jou");
            for (int i = 0; i < n; i++) {
                g2d.drawString(dtFrom.plusDays(i).dayOfWeek().getAsShortText(LOCALE),
                        leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);

                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Hours.hoursBetween(dtFrom.plusDays(i), dtFrom.plusDays(i + 1)).getHours());
            }
            return;
        }


        p = interval.toPeriod(PeriodType.days());
        n = p.getDays();
        unitSize = (int) (settings.graduations.fontMetrics.getStringBounds("30", null)).getWidth();
        if (n < (width / unitSize)) {
            //System.out.println("j");
            for (int i = 0; i < n; i++) {
                g2d.drawString("" + (dtFrom.getDayOfMonth() + i),
                        leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);

                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Hours.hoursBetween(dtFrom.plusDays(i), dtFrom.plusDays(i + 1)).getHours());
            }
            return;
        }

        p = interval.toPeriod(PeriodType.months());
        n = p.getMonths();
        unitSize = (int) (settings.graduations.fontMetrics.getStringBounds("September  ", null)).getWidth();
        if (n < (width / unitSize)) {
            //System.out.println("mois");
            for (int i = 0; i < n; i++) {
                g2d.drawString(dtFrom.plusMonths(i).monthOfYear().getAsText(LOCALE),
                        leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);

                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Days.daysBetween(dtFrom.plusMonths(i), dtFrom.plusMonths(i + 1)).getDays());
            }
            return;
        }


        unitSize = (int) (settings.graduations.fontMetrics.getStringBounds("dec ", null)).getWidth();
        if (n < (width / unitSize)) {
            //System.out.println("mo");
            for (int i = 0; i < n; i++) {
                g2d.drawString(dtFrom.plusMonths(i).monthOfYear().getAsShortText(LOCALE),
                        leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Days.daysBetween(dtFrom.plusMonths(i), dtFrom.plusMonths(i + 1)).getDays());
            }
            return;
        }

        unitSize = (int) (settings.graduations.fontMetrics.getStringBounds("29 ", null)).getWidth();
        if (n < (width / unitSize)) {
            //System.out.println("m");
            for (int i = 0; i < n; i++) {
                g2d.drawString("" + (dtFrom.getMonthOfYear() + i), leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Days.daysBetween(dtFrom.plusMonths(i), dtFrom.plusMonths(i + 1)).getDays());
            }
            return;
        }

        p = interval.toPeriod(PeriodType.years());
        n = p.getYears();
        unitSize = (int) (settings.graduations.fontMetrics.getStringBounds("1980 ", null)).getWidth();
        if (n < (width / unitSize)) {
            //System.out.println("year");
            for (int i = 0; i < n; i++) {
                g2d.drawString("" + (dtFrom.getYear() + i), leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Months.monthsBetween(dtFrom.plusYears(i), dtFrom.plusYears(i + 1)).getMonths());
            }
            return;
        }

        int group = 10;
        n = p.getYears() / group;
        if (n < (width / unitSize)) {
            //System.out.println("10 years");
            for (int i = 0; i < n; i++) {
                g2d.drawString("" + (dtFrom.getYear() + i * group), leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Months.monthsBetween(dtFrom.plusYears(i * group), dtFrom.plusYears((i + 1) * group)).getMonths());
            }
            return;
        }
        group = 20;
        n = p.getYears() / group;
        if (n < (width / unitSize)) {
            //System.out.println("20 years");
            for (int i = 0; i < n; i++) {
                g2d.drawString("" + (dtFrom.getYear() + i * group), leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Months.monthsBetween(dtFrom.plusYears(i * group), dtFrom.plusYears((i + 1) * group)).getMonths());
            }
            return;
        }
        group = 50;
        n = p.getYears() / group;
        if (n < (width / unitSize)) {
            //System.out.println("50 years");
            for (int i = 0; i < n; i++) {
                g2d.drawString("" + (dtFrom.getYear() + i * group), leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Months.monthsBetween(dtFrom.plusYears(i * group), dtFrom.plusYears((i + 1) * group)).getMonths());
            }
            return;
        }
        group = 100;
        n = p.getYears() / group;
        if (n / 100 < (width / unitSize)) {
            //System.out.println("100 years");
            for (int i = 0; i < n; i++) {
                g2d.drawString("" + (dtFrom.getYear() + i * group), leftMargin + 2 + i * (width / n),
                        textTopPosition);
                g2d.drawLine(leftMargin + i * (width / n), 2, leftMargin + i * (width / n), height - settings.graduations.textBottomMargin);
                paintSmallGraduations(g2d,
                        leftMargin + i * (width / n),
                        leftMargin + (i + 1) * (width / n),
                        Months.monthsBetween(dtFrom.plusYears(i * group), dtFrom.plusYears((i + 1) * group)).getMonths());
            }
        }
        return;
    }

    private void paintSmallGraduations(Graphics2D g2d, int x, int y, int numOfGrads) {
        double width = y - x;
        int height = getHeight();
        int topMargin = height - settings.graduations.fontSize - 2;
        int bottomMargin = height - settings.graduations.textBottomMargin;
        int unitSize = 3;

        if (numOfGrads > (width / unitSize)) {
            return;
        }
        for (double i = 1; i < numOfGrads; i++) {
            double xi = x + i * (width / (double) numOfGrads);
            g2d.drawLine((int) xi, topMargin, (int) xi, bottomMargin);
        }
    }

    private boolean inRange(int x, int a, int b) {
        return (a < x && x < b);
    }

    /**
     * Position of current x.
     * @param x current location
     * @param r width of slider
     * @return LOC_RESIZE_*
     */
    private int inPosition(int x, int r) {
        boolean resizeFrom = inRange(x, (int) sf - 1, (int) sf + r + 1);
        boolean resizeTo = inRange(x, (int) st - r - 1, (int) st + 1);
        if (resizeFrom & resizeTo) {
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

    public void mouseClicked(MouseEvent e) {
        latestMousePositionX = e.getX();
        currentMousePositionX = latestMousePositionX;
        //throw new UnsupportedOperationException("Not supported yet.");
        /*
        int w = getWidth();
        // small feature: when the zone is too small, and if we double-click,
        // we want to expand the timeline
        if (e.getClickCount() == 2) {
        if (w != 0) {
        if (sf > 0.1 && st < 0.9) {
        newfrom = 0;
        newto = w;
        repaint();
        }
        }
        
        } else if (e.getClickCount() == 2) {
        if (w != 0) {
        newto = st * (1.0 / w);
        repaint(); // so it will repaint all panels
        }
        
        }
         */
    }

    public void mousePressed(MouseEvent e) {
        if (model == null) {
            return;
        }
        int x = e.getX();
        latestMousePositionX = x;
        currentMousePositionX = latestMousePositionX;
        int r = 16;//skin.getSelectionHookSideLength();

        if (currentState == TimelineState.IDLE) {
            int position = inPosition(x, r);
            switch(position){
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
    }

    public void mouseEntered(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (currentState == TimelineState.IDLE) {
            latestMousePositionX = e.getX();
            currentMousePositionX = latestMousePositionX;
        }
        mouseInside = true;
    }

    public void mouseExited(MouseEvent e) {
        //throw new UnsupportedOperationException("Not supported yet.");
        if (currentState == TimelineState.IDLE) {
            highlightedComponent = HighlightedComponent.NONE;
            latestMousePositionX = e.getX();
            currentMousePositionX = latestMousePositionX;
        }
        mouseInside = false;
        repaint();
    }

    public void timelineAnimatorChanged(ChangeEvent event) {
        repaint();
    }

    public void mouseReleased(MouseEvent evt) {

        latestMousePositionX = evt.getX();
        currentMousePositionX = latestMousePositionX;
        //highlightedComponent = HighlightedComponent.NONE;
        currentState = TimelineState.IDLE;
        this.getParent().repaint(); // so it will repaint upper and bottom panes

    }

    public void mouseMoved(MouseEvent evt) {
        if (model == null) {
            return;
        }

        //System.out.println("mouse moved");
        currentMousePositionX = evt.getX();
        int x = currentMousePositionX;
        float w = getWidth();
        int r = settings.selection.visibleHookWidth;

        // SELECTED ZONE BEGIN POSITION, IN PIXELS
        // int sf = (int) (model.getFromFloat() * (double) w);

        // SELECTED ZONE END POSITION, IN PIXELS
        //int st = (int) (model.getToFloat() * (double) w);

        HighlightedComponent old = highlightedComponent;
        Cursor newCursor = null;

        int a = 0;//settings.selection.invisibleHookMargin;

        int position = inPosition(x, r);
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
        //if (highlightedComponent != old) {
        //    repaint();
        //}
        // now we always repaint, because of the tooltip
        repaint();

    }

    public void mouseDragged(MouseEvent evt) {

        if (model == null) {
            return;
        }
        double w = getWidth();

        currentMousePositionX = evt.getX();
        if (currentMousePositionX > (int) w) {
            currentMousePositionX = (int) w;
        }
        if (currentMousePositionX < 0) {
            currentMousePositionX = 0;
        }
        int x = currentMousePositionX;


        int r = settings.selection.visibleHookWidth;

        // SELECTED ZONE BEGIN POSITION, IN PIXELS
        // sf = (model.getFromFloat() * w);

        // SELECTED ZONE END POSITION, IN PIXELS
        //st = (model.getToFloat() * w);

        if (currentState == TimelineState.IDLE) {
            int position = inPosition(x, r);
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
        double delta = 0;
        if (latestMousePositionX != null) {
            delta = x - latestMousePositionX;
        }
        latestMousePositionX = x;

        // minimal selection zone width (a security to not crush it!)
        int s = settings.selection.minimalWidth;

        switch (currentState) {
            case RESIZE_FROM:

                //problem: moving the left part will crush the security zone
                if ((sf + delta) >= (st - s)) {
                    sf = st - s;
                } else {
                    if (sf + delta <= 0) {
                        sf = 0;
                    } else {
                        sf += delta;
                    }
                }
                break;
            case RESIZE_TO:
                if ((st + delta) <= (sf + s)) {
                    st = sf + s;
                } else {
                    if ((st + delta >= w)) {
                        st = w;
                    } else {
                        st += delta;
                    }
                }
                break;
            case MOVING:
                // collision on the left..
                if ((sf + delta) < 0) {
                    st = (st - sf);
                    sf = 0;
                    // .. or the right
                } else if ((st + delta) >= w) {
                    sf = w - (st - sf);
                    st = w;
                } else {
                    sf += delta;
                    st += delta;
                }
                break;
        }

        if (w != 0) {
            newfrom = sf * (1.0 / w);
            newto = st * (1.0 / w);
        }
        this.repaint(); // so it will repaint all panels

    }
}
