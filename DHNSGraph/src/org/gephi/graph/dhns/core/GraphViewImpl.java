/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.core;

import org.gephi.graph.api.GraphView;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphViewImpl implements GraphView {

    private final int viewId;
    private final TreeStructure structure;
    private final StructureModifier structureModifier;

    public GraphViewImpl(Dhns dhns, int viewId) {
        this.viewId = viewId;
        this.structure = new TreeStructure(viewId);
        this.structureModifier = new StructureModifier(dhns, this);
    }

    public int getViewId() {
        return viewId;
    }

    public TreeStructure getStructure() {
        return structure;
    }

    public StructureModifier getStructureModifier() {
        return structureModifier;
    }

    public boolean isMainView() {
        return viewId == 0;
    }
}
