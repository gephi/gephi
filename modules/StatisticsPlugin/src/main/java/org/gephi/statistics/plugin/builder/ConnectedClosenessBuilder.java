package org.gephi.statistics.plugin.builder;

import org.gephi.statistics.plugin.ConnectedCloseness;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Mathieu Jacomy
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class ConnectedClosenessBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return NbBundle.getMessage(ConnectedClosenessBuilder.class, "ConnectedCloseness.name=Connected-closeness\n");
    }

    @Override
    public Statistics getStatistics() {
        return new ConnectedCloseness();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return ConnectedCloseness.class;
    }
}
