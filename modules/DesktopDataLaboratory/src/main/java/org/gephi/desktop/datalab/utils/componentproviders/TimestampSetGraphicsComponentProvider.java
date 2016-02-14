package org.gephi.desktop.datalab.utils.componentproviders;

import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.graph.api.types.TimeSet;
import org.gephi.graph.api.types.TimestampSet;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author Eduardo Ramos
 */
public class TimestampSetGraphicsComponentProvider extends AbstractTimeSetGraphicsComponentProvider {

    public TimestampSetGraphicsComponentProvider(GraphModelProvider graphModelProvider, JXTable table) {
        super(graphModelProvider, table);
    }

    @Override
    public TimeIntervalGraphicsParameters getTimeIntervalGraphicsParameters(TimeSet value) {
        TimestampSet timestampSet = (TimestampSet) value;

        double[] timestamps = timestampSet.toPrimitiveArray();

        double starts[] = new double[timestamps.length];
        double ends[] = new double[timestamps.length];
        for (int i = 0; i < timestamps.length; i++) {
            starts[i] = timestamps[i];
            ends[i] = timestamps[i];
        }

        return new TimeIntervalGraphicsParameters(starts, ends);
    }
}
