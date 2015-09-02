/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.model.node;

import org.gephi.graph.api.Node;
import org.gephi.visualization.api.initializer.Modeler;
import org.gephi.visualization.opengl.CompatibilityEngine;

/**
 *
 * @author mbastian
 */
public abstract class NodeModeler extends Modeler {

    public NodeModeler(CompatibilityEngine engine) {
        super(engine);
    }

    public abstract NodeModel initModel(Node n);

    public abstract boolean is3d();

    public void setViewportPosition(NodeModel object) {
        float[] res = controller.getDrawable().myGluProject(object.getNode().x(), object.getNode().y(), object.getNode().z());
        object.setViewportX(res[0]);
        object.setViewportY(res[1]);

        res = controller.getDrawable().myGluProject(object.getNode().x() + object.getNode().size(), object.getNode().y(), object.getNode().z());
        float rad = Math.abs((float) res[0] - object.getViewportX());
        object.setViewportRadius(rad);
    }

    protected float cameraDistance(NodeModel object) {
        float[] cameraLocation = controller.getDrawable().getCameraLocation();
        double distance = Math.sqrt(Math.pow((double) object.getNode().x() - cameraLocation[0], 2d)
                + Math.pow((double) object.getNode().y() - cameraLocation[1], 2d)
                + Math.pow((double) object.getNode().z() - cameraLocation[2], 2d));
        object.setCameraDistance((float) distance);

        return (float) distance - object.getNode().size();
    }
}
