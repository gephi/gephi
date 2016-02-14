package org.gephi.desktop.datalab.utils.componentproviders;

import java.util.ArrayList;
import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalMap;
import org.jdesktop.swingx.JXTable;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Eduardo Ramos
 */
public class IntervalMapSparklinesGraphicsComponentProvider extends AbstractSparklinesGraphicsComponentProvider {

    public IntervalMapSparklinesGraphicsComponentProvider(GraphModelProvider graphModelProvider, JXTable table) {
        super(graphModelProvider, table);
    }

    @Override
    public String getTextFromValue(Object value) {
        if (value == null) {
            return null;
        }

        TimeFormat timeFormat = graphModelProvider.getGraphModel().getTimeFormat();
        DateTimeZone timeZone = graphModelProvider.getGraphModel().getTimeZone();

        return ((IntervalMap) value).toString(timeFormat, timeZone);
    }

    @Override
    public Number[][] getSparklinesXAndYNumbers(Object value) {
        IntervalMap intervalMap = (IntervalMap) value;

        ArrayList<Number> xValues = new ArrayList<>();
        ArrayList<Number> yValues = new ArrayList<>();
        if (intervalMap == null) {
            return new Number[2][0];
        }

        Interval[] intervals = intervalMap.toKeysArray();
        Object[] values = intervalMap.toValuesArray();
        Number n;
        for (int i = 0; i < intervals.length; i++) {
            n = (Number) values[i];
            if (n != null) {
                xValues.add(intervals[i].getLow());
                yValues.add(n);
            }
        }

        return new Number[][]{xValues.toArray(new Number[0]), yValues.toArray(new Number[0])};
    }
}
