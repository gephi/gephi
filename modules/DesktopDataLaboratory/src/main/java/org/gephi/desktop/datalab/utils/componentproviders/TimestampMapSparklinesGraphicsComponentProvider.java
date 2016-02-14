package org.gephi.desktop.datalab.utils.componentproviders;

import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.TimestampMap;
import org.jdesktop.swingx.JXTable;
import org.joda.time.DateTimeZone;

/**
 *
 * @author Eduardo Ramos
 */
public class TimestampMapSparklinesGraphicsComponentProvider extends AbstractSparklinesGraphicsComponentProvider {

    public TimestampMapSparklinesGraphicsComponentProvider(GraphModelProvider graphModelProvider, JXTable table) {
        super(graphModelProvider, table);
    }

    @Override
    public String getTextFromValue(Object value) {
        if (value == null) {
            return null;
        }

        TimeFormat timeFormat = graphModelProvider.getGraphModel().getTimeFormat();
        DateTimeZone timeZone = graphModelProvider.getGraphModel().getTimeZone();

        return ((TimestampMap) value).toString(timeFormat, timeZone);
    }

    @Override
    public Number[][] getSparklinesXAndYNumbers(Object value) {
        TimestampMap timestampMap = (TimestampMap) value;

        Double[] timestamps = timestampMap.toKeysArray();
        Number[] values = (Number[]) timestampMap.toValuesArray();

        return new Number[][]{timestamps, values};
    }
}
