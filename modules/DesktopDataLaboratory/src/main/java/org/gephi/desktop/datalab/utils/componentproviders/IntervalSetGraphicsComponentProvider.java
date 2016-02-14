package org.gephi.desktop.datalab.utils.componentproviders;

import org.gephi.desktop.datalab.utils.GraphModelProvider;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimeSet;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author Eduardo Ramos
 */
public class IntervalSetGraphicsComponentProvider extends AbstractTimeSetGraphicsComponentProvider {

    public IntervalSetGraphicsComponentProvider(GraphModelProvider graphModelProvider, JXTable table) {
        super(graphModelProvider, table);
    }

    @Override
    public TimeIntervalGraphicsParameters getTimeIntervalGraphicsParameters(TimeSet value) {
        IntervalSet intervalSet = (IntervalSet) value;
        double[] intervals = intervalSet.getIntervals();

        double starts[] = new double[intervals.length / 2];
        double ends[] = new double[intervals.length / 2];
        for (int i = 0, startIndex = 0; startIndex < intervals.length; i++, startIndex += 2) {
            starts[i] = intervals[startIndex];
            ends[i] = intervals[startIndex + 1];
        }

        return new TimeIntervalGraphicsParameters(starts, ends);
    }
}
