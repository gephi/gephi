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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Mathieu Bastian
 */
public class DateTick {

    // Consts
    protected final static int MIN_PIXELS = 10;
    // Fields
    private final Instant min;
    private final Instant max;
    private final TickPeriod[] tickPeriods;

    public DateTick(Instant min, Instant max, ChronoUnit[] units) {
        this.min = min;
        this.max = max;
        this.tickPeriods = new TickPeriod[units.length];
        for (int i = 0; i < units.length; i++) {
            this.tickPeriods[i] = new TickPeriod(min, max, units[i]);
        }
    }

    public static DateTick create(double min, double max, int width) {
        Instant minInstant = Instant.ofEpochMilli((long) min);
        Instant maxInstant = Instant.ofEpochMilli((long) max);

        Duration duration = Duration.between(minInstant, maxInstant);
        long days = duration.toDays();
        long hours = duration.toHours();
        long minutes = duration.toMinutes();
        long seconds = duration.getSeconds();

        // Top unit
        ChronoUnit topUnit;
        if (days > 0) {
            topUnit = ChronoUnit.DAYS;
        } else if (hours > 0) {
            topUnit = ChronoUnit.HOURS;
        } else if (minutes > 0) {
            topUnit = ChronoUnit.MINUTES;
        } else if (seconds > 0) {
            topUnit = ChronoUnit.SECONDS;
        } else {
            topUnit = ChronoUnit.MILLIS;
        }

        // Bottom unit
        if (topUnit != ChronoUnit.MILLIS) {
            ChronoUnit bottomUnit;
            if (topUnit == ChronoUnit.DAYS) {
                bottomUnit = ChronoUnit.HOURS;
            } else if (topUnit == ChronoUnit.HOURS) {
                bottomUnit = ChronoUnit.MINUTES;
            } else if (topUnit == ChronoUnit.MINUTES) {
                bottomUnit = ChronoUnit.SECONDS;
            } else {
                bottomUnit = ChronoUnit.MILLIS;
            }

            // Number of ticks
            long intervals = duration.toMillis() / bottomUnit.getDuration().toMillis();
            if (intervals > 0) {
                int intervalSize = width / (int) intervals;
                if (intervalSize >= MIN_PIXELS) {
                    return new DateTick(minInstant, maxInstant, new ChronoUnit[] { topUnit, bottomUnit });
                }
            }
        }

        return new DateTick(minInstant, maxInstant, new ChronoUnit[] { topUnit });
    }

    public int getTypeCount() {
        return tickPeriods.length;
    }

    public List<Interval> getIntervals(int type) {
        return tickPeriods[type].getIntervals();
    }

    public String getTickValue(int type, Instant instant) {
        return tickPeriods[type].getTickValue(instant);
    }

    public ChronoUnit getDurationType(int type) {
        return tickPeriods[type].getDurationType();
    }

    public int getTickPixelPosition(long ms, int width) {
        long minMs = min.toEpochMilli();
        long maxMs = max.toEpochMilli();
        long duration = maxMs - minMs;
        return (int) ((ms - minMs) * width / duration);
    }

    private static class TickPeriod {

        protected final Instant min;
        protected final Instant max;
        protected final Duration period;
        protected final Instant interval;
        protected final ChronoUnit unit;

        public TickPeriod(Instant min, Instant max, ChronoUnit unit) {
            this.min = min;
            this.max = max;
            this.period = Duration.between(min, max);
            this.interval = min;
            this.unit = unit;
        }

        public List<Interval> getIntervals() {
            long totalIntervals = period.toMillis() / unit.getDuration().toMillis() + 2;
            List<Interval> intervals = new ArrayList<>();
            for (int i = 0; i < totalIntervals; i++) {
                Instant currentInterval;
                if (i == 0) {
                    currentInterval = min;
                } else {
                    currentInterval = min.plus(i, unit);
                }
                intervals.add(new Interval(currentInterval, currentInterval.plus(1, unit)));
            }
            return intervals;
        }

        public int getIntervalCount() {
            return (int) (period.toMillis() / unit.getDuration().toMillis());
        }

        public String getTickValue(Instant instant) {
            return instant.toString();
        }

        public ChronoUnit getDurationType() {
            return unit;
        }
    }

    public static class Interval {
        private final Instant start;
        private final Instant end;

        public Interval(Instant start, Instant end) {
            this.start = start;
            this.end = end;
        }

        public Instant getStart() {
            return start;
        }

        public Instant getEnd() {
            return end;
        }
    }
}