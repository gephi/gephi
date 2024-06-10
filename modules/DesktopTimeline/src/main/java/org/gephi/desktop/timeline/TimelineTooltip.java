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

import java.awt.Point;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JComponent;
import org.gephi.timeline.api.TimelineChart;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.ui.components.richtooltip.RichTooltip;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author Mathieu Bastian
 */
public class TimelineTooltip {

    private static final int DELAY = 500;
    private TimelineModel model;
    private String position;
    private String min;
    private String max;
    private String y;
    private Timer timer;
    private RichTooltip tooltip;
    private final Lock lock = new ReentrantLock();

    public void setModel(TimelineModel model) {
        this.model = model;
    }

    public void start(final double currentPosition, final Point mousePosition, final JComponent component) {
        stop();
        if (model == null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                lock.lock();
                try {
                    if (tooltip != null) {
                        tooltip.hideTooltip();
                    }
                    buildData(currentPosition);
                    tooltip = buildTooltip();
                    tooltip.showTooltip(component, mousePosition);
                } finally {
                    lock.unlock();
                }
            }
        }, TimelineTooltip.DELAY);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
        lock.lock();
        try {
            if (tooltip != null) {
                tooltip.hideTooltip();
            }
        } finally {
            lock.unlock();
        }
        timer = null;
        tooltip = null;
    }

    private void buildData(double currentPosition) {
        switch (model.getTimeFormat()) {
            case DOUBLE:
                int exponentMin = (int) Math.round(Math.log10(model.getCustomMin()));
                DecimalFormat decimalFormat = new DecimalFormat();
                decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
                if (exponentMin > 0) {
                    min = String.valueOf(model.getCustomMin());
                    max = String.valueOf(model.getCustomMax());
                    position = String.valueOf(currentPosition);
                } else {
                    decimalFormat.setMaximumFractionDigits(Math.abs(exponentMin) + 2);
                    min = decimalFormat.format(model.getCustomMin());
                    max = decimalFormat.format(model.getCustomMax());
                    position = decimalFormat.format(currentPosition);
                }
                break;
            case DATE: {
                LocalDateTime minDate = Instant.ofEpochMilli((long) model.getCustomMin()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime maxDate = Instant.ofEpochMilli((long) model.getCustomMax()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime posDate = Instant.ofEpochMilli((long) currentPosition).atZone(ZoneId.systemDefault()).toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
                min = formatter.format(minDate);
                max = formatter.format(maxDate);
                position = formatter.format(posDate);
                break;
            }
            default: {
                LocalDateTime minDate = Instant.ofEpochMilli((long) model.getCustomMin()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime maxDate = Instant.ofEpochMilli((long) model.getCustomMax()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime posDate = Instant.ofEpochMilli((long) currentPosition).atZone(ZoneId.systemDefault()).toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                min = formatter.format(minDate);
                max = formatter.format(maxDate);
                position = formatter.format(posDate);
                break;
            }
        }

        if (model.getChart() != null) {
            TimelineChart chart = model.getChart();
            Number yNumber = chart.getY(currentPosition);
            y = yNumber != null ? yNumber.toString() : null;
        } else {
            y = null;
        }
    }

    private RichTooltip buildTooltip() {
        RichTooltip richTooltip = new RichTooltip();

        //Min
        richTooltip
            .addDescriptionSection(NbBundle.getMessage(TimelineTooltip.class, "TimelineTooltip.min") + ": " + getMin());

        //Max
        richTooltip
            .addDescriptionSection(NbBundle.getMessage(TimelineTooltip.class, "TimelineTooltip.max") + ": " + getMax());

        //Title
        richTooltip.setTitle(getPosition());

        //Img
        richTooltip.setMainImage(ImageUtilities.loadImage("DesktopTimeline/info.png", false));

        //Chart
        if (getY() != null) {
            richTooltip.addFooterSection(model.getChart().getColumn());
            richTooltip
                .addFooterSection(NbBundle.getMessage(TimelineTooltip.class, "TimelineTooltip.chart") + ": " + getY());

            //Img
            richTooltip.setFooterImage(ImageUtilities.loadImage("DesktopTimeline/chart.png", false));
        }

        return richTooltip;
    }

    public String getY() {
        return y;
    }

    public String getPosition() {
        return position;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }
}
