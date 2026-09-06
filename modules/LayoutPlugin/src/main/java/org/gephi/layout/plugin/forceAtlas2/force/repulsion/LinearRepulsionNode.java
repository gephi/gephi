package org.gephi.layout.plugin.forceAtlas2.force.repulsion;

import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData;
import org.gephi.layout.plugin.forceAtlas2.force.AFA2Force;

/**
 * @author totetmatt
 */
public class LinearRepulsionNode extends AFA2Force implements IRepulsionNode {

    public LinearRepulsionNode(ForceAtlas2.ForceAtlas2Params params) {
        super(params);
    }

    @Override
    public void accept(Node n1, Node n2) {
        ForceAtlas2LayoutData n1Layout = n1.getLayoutData();
        ForceAtlas2LayoutData n2Layout = n2.getLayoutData();

        // Get the distance
        double xDist = n1.x() - n2.x();
        double yDist = n1.y() - n2.y();
        double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

        if (distance > 0) {
            // NB: factor = force / distance
            double factor = params.scalingRatio * n1Layout.mass * n2Layout.mass / distance / distance;

            n1Layout.dx += xDist * factor;
            n1Layout.dy += yDist * factor;

            n2Layout.dx -= xDist * factor;
            n2Layout.dy -= yDist * factor;
        }
    }

}
