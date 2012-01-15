/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author mbastian
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

        DateTime minDate = new DateTime("2009-11-17T20:00:00");
        DateTime maxDate = new DateTime("2009-11-18T23:00:00");

        Period period = new Period(minDate, maxDate, PeriodType.yearMonthDayTime());;
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
                    currentInterval = min.property(type).toInterval();;
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
