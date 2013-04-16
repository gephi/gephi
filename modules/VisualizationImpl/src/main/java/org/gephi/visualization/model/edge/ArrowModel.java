/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.model.edge;

import org.gephi.visualization.model.Model;

/**
 *
 * @author mbastian
 */
public abstract class ArrowModel implements Model {

    protected final EdgeModel edgeModel;
    public boolean mark;

    public ArrowModel(EdgeModel edge) {
        this.edgeModel = edge;
    }

    public EdgeModel getEdgeModel() {
        return edgeModel;
    }
}
