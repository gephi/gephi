package org.gephi.layout.plugin.forceAtlas2.force.repulsion;

import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData;
import org.gephi.layout.plugin.forceAtlas2.Region;
import org.gephi.layout.plugin.forceAtlas2.force.AFA2Force;

/**
 *
 * @author totetmatt
 */
public class LinearRepulsionRegionAntiCollision extends AFA2Force implements IRepulsionRegion {

    public LinearRepulsionRegionAntiCollision(ForceAtlas2.ForceAtlas2Params params) {
        super(params);
    }

    @Override
    public void accept(Node n, Region r) {
        ForceAtlas2LayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x() - r.getMassCenterX();
            double yDist = n.y() - r.getMassCenterY();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = params.scalingRatio * nLayout.mass * r.getMass() / distance / distance;

                nLayout.dx += xDist * factor;
                nLayout.dy += yDist * factor;
            } else if (distance < 0) {
                double factor = -params.scalingRatio * nLayout.mass * r.getMass() / distance;

                nLayout.dx += xDist * factor;
                nLayout.dy += yDist * factor;
            } 
    }
    
}
