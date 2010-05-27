/*
 * Author: Patrick J. McSweeney
 * Syracuse University
 */

package org.gephi.statistics.plugin.builder;

import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author pjmcswee
 */
@ServiceProvider(service=StatisticsBuilder.class)
public class ConnectedComponentsBuilder implements StatisticsBuilder {

    public String getName() {
        return NbBundle.getMessage(ConnectedComponentsBuilder.class, "ConnectedComponents.name");
    }

    public Statistics getStatistics() {
        return new ConnectedComponents();
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return ConnectedComponents.class;
    }
}
