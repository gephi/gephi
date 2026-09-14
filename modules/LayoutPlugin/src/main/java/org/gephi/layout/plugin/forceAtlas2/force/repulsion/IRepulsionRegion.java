package org.gephi.layout.plugin.forceAtlas2.force.repulsion;

import java.util.function.BiConsumer;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.Region;

/**
 * @author totetmatt
 */
public interface IRepulsionRegion extends BiConsumer<Node, Region> {

}
