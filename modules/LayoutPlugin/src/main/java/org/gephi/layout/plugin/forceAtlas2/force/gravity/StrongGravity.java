package org.gephi.layout.plugin.forceAtlas2.force.gravity;


import org.gephi.layout.plugin.forceAtlas2.force.gravity.IGravity;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData;
import org.gephi.layout.plugin.forceAtlas2.force.AFA2Force;

/**
 * @author totetmatt
 */
public class StrongGravity extends AFA2Force implements IGravity {

    public StrongGravity(ForceAtlas2.ForceAtlas2Params params) {
        super(params);
    }

    @Override
    public void accept(Node n, Double g) {
        ForceAtlas2LayoutData nLayout = n.getLayoutData();
        // Get the distance
        double xDist = n.x();
        double yDist = n.y();
        double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);
        if (distance > 0) {
            // NB: factor = force / distance
            double factor = params.scalingRatio * nLayout.mass * g;
            nLayout.dx -= xDist * factor;
            nLayout.dy -= yDist * factor;
        }
    }


}
