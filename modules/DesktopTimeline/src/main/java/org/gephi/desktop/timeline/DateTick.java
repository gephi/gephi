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

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.PeriodType;

/**
 *
 * @author Mathieu Bastian
 */
public class DateTick {

    //Consts
    protected final static int MIN_PIXELS = 10;
    //Fields
    private final DateTime min;
    private final DateTime max;
    private final TickPeriod[] tickPeriods;

    public DateTick(DateTime min, DateTime max, DateTimeFieldType[] types) {
        this.min = min;
        this.max = max;
        this.tickPeriods = new TickPeriod[types.length];
        for (int i = 0; i < types.length; i++) {
            this.tickPeriods[i] = new TickPeriod(min, max, types[i]);
        }
    }

    public int getTypeCount() {
        return tickPeriods.length;
    }

    public Interval[] getIntervals(int type) {
        return tickPeriods[type].getIntervals();
    }

    public String getTickValue(int type, DateTime dateTime) {
        return tickPeriods[type].getTickValue(dateTime);
    }

    public DurationFieldType getDurationType(int type) {
        return tickPeriods[type].getDurationType();
    }

    public int getTickPixelPosition(long ms, int width) {
        long minMs = min.getMillis();
        long maxMs = max.getMillis();
        long duration = maxMs - minMs;
        return (int) ((ms - minMs) * width / duration);
    }

    public static DateTick create(double min, double max, int width) {

        DateTime minDate = new DateTime((long) min);
        DateTime maxDate = new DateTime((long) max);

        Period period = new Period(minDate, maxDate, PeriodType.yearMonthDayTime());
        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();
        int hours = period.getHours();
        int minutes = period.getMinutes();
        int seconds = period.getSeconds();

        //Top type
        DateTimeFieldType topType;
        if (years > 0) {
            topType = DateTimeFieldType.year();
        } else if (months > 0) {
            topType = DateTimeFieldType.monthOfYear();
        } else if (days > 0) {
            topType = DateTimeFieldType.dayOfMonth();
        } else if (hours > 0) {
            topType = DateTimeFieldType.hourOfDay();
        } else if (minutes > 0) {
            topType = DateTimeFieldType.minuteOfHour();
        } else if (seconds > 0) {
            topType = DateTimeFieldType.secondOfMinute();
        } else {
            topType = DateTimeFieldType.millisOfSecond();
        }

        //Bottom type
        if (topType != DateTimeFieldType.millisOfSecond()) {
            DateTimeFieldType bottomType;
            if (topType.equals(DateTimeFieldType.year())) {
                bottomType = DateTimeFieldType.monthOfYear();
            } else if (topType.equals(DateTimeFieldType.monthOfYear())) {
                bottomType = DateTimeFieldType.dayOfMonth();
            } else if (topType.equals(DateTimeFieldType.dayOfMonth())) {
                bottomType = DateTimeFieldType.hourOfDay();
            } else if (topType.equals(DateTimeFieldType.hourOfDay())) {
                bottomType = DateTimeFieldType.minuteOfHour();
            } else if (topType.equals(DateTimeFieldType.minuteOfHour())) {
                bottomType = DateTimeFieldType.secondOfMinute();
            } else {
                bottomType = DateTimeFieldType.millisOfSecond();
            }

            //Number of ticks
            Period p = new Period(minDate, maxDate, PeriodType.forFields(new DurationFieldType[]{bottomType.getDurationType()}));
            int intervals = p.get(bottomType.getDurationType());
            if (intervals > 0) {
                int intervalSize = width / intervals;
                if (intervalSize >= MIN_PIXELS) {
                    return new DateTick(minDate, maxDate, new DateTimeFieldType[]{topType, bottomType});
                }
            }
        }

        return new DateTick(minDate, maxDate, new DateTimeFieldType[]{topType});
    }

    private static class TickPeriod {

        protected final DateTime min;
        protected final DateTime max;
        protected final Period period;
        protected final Interval interval;
        protected final DateTimeFieldType type;

        public TickPeriod(DateTime min, DateTime max, DateTimeFieldType type) {
            this.min = min;
            this.max = max;
            this.period = new Period(min, max, PeriodType.forFields(new DurationFieldType[]{type.getDurationType()}));
            this.interval = new Interval(min, max);
            this.type = type;
        }

        public Interval[] getIntervals() {
            int totalIntervals = period.get(type.getDurationType()) + 2;
            Interval[] intervals = new Interval[totalIntervals];
            for (int i = 0; i < totalIntervals; i++) {
                Interval currentInterval;
                if (i == 0) {
                    currentInterval = min.property(type).toInterval();
                } else {
                    currentInterval = min.property(type).addToCopy(i).property(type).toInterval();
                }
                intervals[i] = currentInterval;
            }
            return intervals;
        }

        public int getIntervalCount() {
            return period.get(type.getDurationType());
        }

        public String getTickValue(DateTime dateTime) {
            return dateTime.property(type).getAsShortText();
        }

        public DurationFieldType getDurationType() {
            return type.getDurationType();
        }
    }
}
