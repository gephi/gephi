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
    private int edgesCountTotal;
    private int mutualEdgesTotal;
    private int edgesCountEnabled;
    private int mutualEdgesEnabled;

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

    public void incEdgesCountTotal(int shift) {
        edgesCountTotal += shift;
    }

    public void incEdgesCountEnabled(int shift) {
        edgesCountEnabled += shift;
    }

    public void incMutualEdgesTotal(int shift) {
        mutualEdgesTotal += shift;
    }

    public void incMutualEdgesEnabled(int shift) {
        mutualEdgesEnabled += shift;
    }

    public void decEdgesCountTotal(int shift) {
        edgesCountTotal -= shift;
    }

    public void decEdgesCountEnabled(int shift) {
        edgesCountEnabled -= shift;
    }

    public void decMutualEdgesTotal(int shift) {
        mutualEdgesTotal -= shift;
    }

    public void decMutualEdgesEnabled(int shift) {
        mutualEdgesEnabled -= shift;
    }

    public int getEdgesCountEnabled() {
        return edgesCountEnabled;
    }

    public void setEdgesCountEnabled(int edgesCountEnabled) {
        this.edgesCountEnabled = edgesCountEnabled;
    }

    public int getEdgesCountTotal() {
        return edgesCountTotal;
    }

    public void setEdgesCountTotal(int edgesCountTotal) {
        this.edgesCountTotal = edgesCountTotal;
    }

    public int getMutualEdgesEnabled() {
        return mutualEdgesEnabled;
    }

    public void setMutualEdgesEnabled(int mutualEdgesEnabled) {
        this.mutualEdgesEnabled = mutualEdgesEnabled;
    }

    public int getMutualEdgesTotal() {
        return mutualEdgesTotal;
    }

    public void setMutualEdgesTotal(int mutualEdgesTotal) {
        this.mutualEdgesTotal = mutualEdgesTotal;
    }
}
