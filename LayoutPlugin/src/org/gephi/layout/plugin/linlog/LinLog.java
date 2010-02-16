/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.layout.plugin.linlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

/**
 *
 * @author Andreas Noack
 */
public class LinLog extends AbstractLayout implements Layout {

    //Graph
    protected Graph graph;
    //Properties
    public int attractionPower = 0;
    public int repulsionPower = -1;
    public double maxDisplacement = 50d;
    public double gravity = 0d;
    public double speed = 100d;
    public boolean edgeRepulsion = true;
    public HashMap<Integer, Double> edgeRepulsionWeights;

    public LinLog(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    public void resetPropertiesValues() {
        setAttractionPower(0);
        setRepulsionPower(-1);
        setMaxDisplacement(50d);
        setGravity(0d);
        setSpeed(100d);
        setEdgeRepulsion(true);
    }

    public void initAlgo() {
        this.graph = graphModel.getGraphVisible();
        for (Node n : graph.getNodes()) {
            n.getNodeData().setLayoutData(new LinLogLayoutData());
        }
        edgeRepulsionWeights = new HashMap();
        for (Node n : graph.getNodes()) {
            Double weight = 0d;
            for (Edge e : ((DirectedGraph) graph).getOutEdges(n)) {
                weight += edgeWeight(e);
            }
            edgeRepulsionWeights.put(n.getId(), weight);
        }
    }

    public void goAlgo() {
        this.graph = graphModel.getGraphVisible();
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();

        for (Node n : nodes) {
            if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof LinLogLayoutData)) {
                n.getNodeData().setLayoutData(new LinLogLayoutData());
            }
            LinLogLayoutData llld = n.getNodeData().getLayoutData();
            llld.dx = 0;
            llld.dy = 0;
        }

        // repulsion
        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 != n2) {
                    LinLogLayoutData ld1 = n1.getNodeData().getLayoutData();
                    LinLogLayoutData ld2 = n2.getNodeData().getLayoutData();

                    double xDist = n1.getNodeData().x() - n2.getNodeData().x();
                    double yDist = n1.getNodeData().y() - n2.getNodeData().y();
                    double distance = Math.max((float) Math.sqrt(xDist * xDist + yDist * yDist), Float.MIN_VALUE);

                    double force;
                    if (repulsionPower == 0.0) {
                        force = nodeWeight(n1) * nodeWeight(n2) * Math.log(distance);
                    } else {
                        force = nodeWeight(n1) * nodeWeight(n2) * Math.pow(distance, repulsionPower);
                    }
                    ld1.dx += (float) (xDist / distance * force);
                    ld1.dy += (float) (yDist / distance * force);

                    ld2.dx -= (float) (xDist / distance * force);
                    ld2.dy -= (float) (yDist / distance * force);
                }
            }
        }
        // attraction
        for (Edge e : edges) {
            Node n1 = e.getSource();
            Node n2 = e.getTarget();
            if (n1 != n2) {
                LinLogLayoutData ld1 = n1.getNodeData().getLayoutData();
                LinLogLayoutData ld2 = n2.getNodeData().getLayoutData();

                double xDist = n1.getNodeData().x() - n2.getNodeData().x();
                double yDist = n1.getNodeData().y() - n2.getNodeData().y();
                double distance = Math.max((float) Math.sqrt(xDist * xDist + yDist * yDist), Float.MIN_VALUE);

                double force;
                if (attractionPower == 0.0) {
                    force = edgeWeight(e) * Math.log(distance);
                } else {
                    force = edgeWeight(e) * Math.pow(distance, attractionPower) / attractionPower;
                }

                ld1.dx -= (float) (xDist / distance * force);
                ld1.dy -= (float) (yDist / distance * force);

                ld2.dx += (float) (xDist / distance * force);
                ld2.dy += (float) (yDist / distance * force);
            }
        }
        // gravity
        if (gravity > 0) {
            for (Node n : nodes) {
                double xDist = n.getNodeData().x();
                double yDist = n.getNodeData().y();
                double distance = Math.max((float) Math.sqrt(xDist * xDist + yDist * yDist), Float.MIN_VALUE);

                double force;
                if (attractionPower == 0.0) {
                    force = gravity * nodeWeight(n) * Math.log(distance);
                } else {
                    force = gravity * nodeWeight(n) * Math.pow(distance, attractionPower) / attractionPower;
                }

                LinLogLayoutData layoutData = n.getNodeData().getLayoutData();
                layoutData.dx -= force * xDist / distance;
                layoutData.dy -= force * yDist / distance;
            }
        }
        for (Node n : nodes) {
            LinLogLayoutData layoutData = n.getNodeData().getLayoutData();
            Double realSpeed = (0.1d * speed) * (0.1d * speed);
            layoutData.dx *= realSpeed;
            layoutData.dy *= realSpeed;
        }

        // apply forces
        for (Node n : nodes) {
            NodeData nData = n.getNodeData();
            LinLogLayoutData nLayout = nData.getLayoutData();
            if (!nData.isFixed()) {
                double d = 0.0001 + Math.sqrt(nLayout.dx * nLayout.dx + nLayout.dy * nLayout.dy);
                float ratio = (float) Math.min(1, getMaxDisplacement() / d);
                nLayout.dx *= ratio;
                nLayout.dy *= ratio;
                float x = nData.x() + nLayout.dx;
                float y = nData.y() + nLayout.dy;

                nData.setX(x);
                nData.setY(y);
            }
        }
    }

    public void endAlgo() {
    }

    @Override
    public boolean canAlgo() {
        return true;
    }

    private double nodeWeight(Node n) {
        if (edgeRepulsion) {
            return edgeRepulsionWeights.get(n.getId());
        } else {
            return 1;
        }
    }

    private double edgeWeight(Edge e) {
        return e.getWeight();
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String LINLOG = "LinLog";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    NbBundle.getMessage(LinLog.class, "linLog.repulsionPower.name"),
                    LINLOG,
                    NbBundle.getMessage(LinLog.class, "linLog.repulsionPower.desc"),
                    "getRepulsionPower", "setRepulsionPower"));
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    NbBundle.getMessage(LinLog.class, "linLog.attractionPower.name"),
                    LINLOG,
                    NbBundle.getMessage(LinLog.class, "linLog.attractionPower.desc"),
                    "getAttractionPower", "setAttractionPower"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(LinLog.class, "linLog.maxDisplacement.name"),
                    LINLOG,
                    NbBundle.getMessage(LinLog.class, "linLog.maxDisplacement.desc"),
                    "getMaxDisplacement", "setMaxDisplacement"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(LinLog.class, "linLog.gravity.name"),
                    LINLOG,
                    NbBundle.getMessage(LinLog.class, "linLog.gravity.desc"),
                    "getGravity", "setGravity"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(LinLog.class, "linLog.speed.name"),
                    LINLOG,
                    NbBundle.getMessage(LinLog.class, "linLog.speed.desc"),
                    "getSpeed", "setSpeed"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(LinLog.class, "linLog.edgeRepulsion.name"),
                    LINLOG,
                    NbBundle.getMessage(LinLog.class, "linLog.edgeRepulsion.desc"),
                    "isEdgeRepulsion", "setEdgeRepulsion"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public void setAttractionPower(Integer attractionPower) {
        this.attractionPower = attractionPower;
    }

    public int getAttractionPower() {
        return attractionPower;
    }

    public void setRepulsionPower(Integer repulsionPower) {
        this.repulsionPower = repulsionPower;
    }

    public int getRepulsionPower() {
        return repulsionPower;
    }

    public void setMaxDisplacement(Double maxDisplacement) {
        this.maxDisplacement = maxDisplacement;
    }

    public Double getMaxDisplacement() {
        return maxDisplacement;
    }

    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    public Double getGravity() {
        return gravity;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setEdgeRepulsion(Boolean edgeRepulsion) {
        this.edgeRepulsion = edgeRepulsion;
    }

    public Boolean isEdgeRepulsion() {
        return edgeRepulsion;
    }
}

