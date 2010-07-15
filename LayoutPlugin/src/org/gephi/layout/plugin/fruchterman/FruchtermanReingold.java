/*
Copyright 2008-2010 Gephi
Authors : Mathieu Jacomy
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.layout.plugin.fruchterman;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.ForceVectorNodeLayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Jacomy
 */
public class FruchtermanReingold extends AbstractLayout implements Layout {

    private static final float SPEED_DIVISOR = 800;
    private static final float AREA_MULTIPLICATOR = 10000;
    //Graph
    protected HierarchicalGraph graph;
    //Properties
    private float area;
    private double gravity;
    private double speed;

    public FruchtermanReingold(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void resetPropertiesValues() {
        speed = 1;
        area = 10000;
        gravity = 10;
    }

    public void initAlgo() {
        this.graph = graphModel.getHierarchicalGraphVisible();
        for (Node n : graph.getNodes()) {
            n.getNodeData().setLayoutData(new ForceVectorNodeLayoutData());
        }
    }

    public void goAlgo() {
        this.graph = graphModel.getHierarchicalGraphVisible();
        graph.readLock();
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdgesAndMetaEdges().toArray();

        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof ForceVectorNodeLayoutData)) {
                n.getNodeData().setLayoutData(new ForceVectorNodeLayoutData());
            }
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.dx = 0;
            layoutData.dy = 0;
        }

        float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * area) / 10f);					// Déplacement limite : on peut le calibrer...
        float k = (float) Math.sqrt((AREA_MULTIPLICATOR * area) / (1f + nodes.length));		// La variable k, l'idée principale du layout.

        for (Node N1 : nodes) {
            for (Node N2 : nodes) {	// On fait toutes les paires de noeuds
                if (N1 != N2) {
                    float xDist = N1.getNodeData().x() - N2.getNodeData().x();	// distance en x entre les deux noeuds
                    float yDist = N1.getNodeData().y() - N2.getNodeData().y();
                    float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);	// distance tout court

                    if (dist > 0) {
                        float repulsiveF = k * k / dist;			// Force de répulsion
                        ForceVectorNodeLayoutData layoutData = N1.getNodeData().getLayoutData();
                        layoutData.dx += xDist / dist * repulsiveF;		// on l'applique...
                        layoutData.dy += yDist / dist * repulsiveF;
                    }
                }
            }
        }
        for (Edge E : edges) {
            // Idem, pour tous les noeuds on applique la force d'attraction

            Node Nf = E.getSource();
            Node Nt = E.getTarget();

            float xDist = Nf.getNodeData().x() - Nt.getNodeData().x();
            float yDist = Nf.getNodeData().y() - Nt.getNodeData().y();
            float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            float attractiveF = dist * dist / k;

            if (dist > 0) {
                ForceVectorNodeLayoutData sourceLayoutData = Nf.getNodeData().getLayoutData();
                ForceVectorNodeLayoutData targetLayoutData = Nt.getNodeData().getLayoutData();
                sourceLayoutData.dx -= xDist / dist * attractiveF;
                sourceLayoutData.dy -= yDist / dist * attractiveF;
                targetLayoutData.dx += xDist / dist * attractiveF;
                targetLayoutData.dy += yDist / dist * attractiveF;
            }
        }
        // gravity
        for (Node n : nodes) {
            NodeData nodeData = n.getNodeData();
            ForceVectorNodeLayoutData layoutData = nodeData.getLayoutData();
            float d = (float) Math.sqrt(nodeData.x() * nodeData.x() + nodeData.y() * nodeData.y());
            float gf = 0.01f * k * (float) gravity * d;
            layoutData.dx -= gf * nodeData.x() / d;
            layoutData.dy -= gf * nodeData.y() / d;
        }
        // speed
        for (Node n : nodes) {
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            layoutData.dx *= speed / SPEED_DIVISOR;
            layoutData.dy *= speed / SPEED_DIVISOR;
        }
        for (Node n : nodes) {
            // Maintenant on applique le déplacement calculé sur les noeuds.
            // nb : le déplacement à chaque passe "instantanné" correspond à la force : c'est une sorte d'accélération.
            ForceVectorNodeLayoutData layoutData = n.getNodeData().getLayoutData();
            float xDist = layoutData.dx;
            float yDist = layoutData.dy;
            float dist = (float) Math.sqrt(layoutData.dx * layoutData.dx + layoutData.dy * layoutData.dy);
            if (dist > 0 && !n.getNodeData().isFixed()) {
                float limitedDist = Math.min(maxDisplace * ((float) speed / SPEED_DIVISOR), dist);
                n.getNodeData().setX(n.getNodeData().x() + xDist / dist * limitedDist);
                n.getNodeData().setY(n.getNodeData().y() + yDist / dist * limitedDist);
            }
        }
        graph.readUnlock();
    }

    public void endAlgo() {
        for (Node n : graph.getNodes()) {
            n.getNodeData().setLayoutData(null);
        }
    }

    @Override
    public boolean canAlgo() {
        return true;
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String FRUCHTERMAN_REINGOLD = "Fruchterman Reingold";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(FruchtermanReingold.class, "fruchtermanReingold.area.name"),
                    FRUCHTERMAN_REINGOLD,
                    NbBundle.getMessage(FruchtermanReingold.class, "fruchtermanReingold.area.desc"),
                    "getArea", "setArea"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(FruchtermanReingold.class, "fruchtermanReingold.gravity.name"),
                    FRUCHTERMAN_REINGOLD,
                    NbBundle.getMessage(FruchtermanReingold.class, "fruchtermanReingold.gravity.desc"),
                    "getGravity", "setGravity"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(FruchtermanReingold.class, "fruchtermanReingold.speed.name"),
                    FRUCHTERMAN_REINGOLD,
                    NbBundle.getMessage(FruchtermanReingold.class, "fruchtermanReingold.speed.desc"),
                    "getSpeed", "setSpeed"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public Float getArea() {
        return area;
    }

    public void setArea(Float area) {
        this.area = area;
    }

    /**
     * @return the gravity
     */
    public Double getGravity() {
        return gravity;
    }

    /**
     * @param gravity the gravity to set
     */
    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    /**
     * @return the speed
     */
    public Double getSpeed() {
        return speed;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(Double speed) {
        this.speed = speed;
    }
}
