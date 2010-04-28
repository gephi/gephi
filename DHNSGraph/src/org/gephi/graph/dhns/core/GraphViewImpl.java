/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.core;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.dhns.graph.AbstractGraphImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphViewImpl implements GraphView {

    private final Dhns dhns;
    private final int viewId;
    private final TreeStructure structure;
    private final StructureModifier structureModifier;
    private int nodesEnabled;
    private int edgesCountTotal;
    private int mutualEdgesTotal;
    private int edgesCountEnabled;
    private int mutualEdgesEnabled;
    //RefCounting
    private final WeakHashMap<AbstractGraphImpl, Boolean> graphsMap = new WeakHashMap<AbstractGraphImpl, Boolean>();

    public GraphViewImpl(Dhns dhns, int viewId) {
        this.dhns = dhns;
        this.viewId = viewId;
        this.structure = new TreeStructure(viewId);
        this.structureModifier = new StructureModifier(dhns, this);
    }

    public void addGraphReference(AbstractGraphImpl graph) {
        graphsMap.put(graph, Boolean.TRUE);

        //Track graph references
        /*StackTraceElement[] elm = Thread.currentThread().getStackTrace();
        int i;
        for (i = 1; i < elm.length; i++) {
        if (!elm[i].getClassName().startsWith("org.gephi.graph")) {
        break;
        }
        }
        System.out.println("View " + viewId + " : " + elm[i].toString());*/
    }

    public boolean hasGraphReference() {
        return !graphsMap.isEmpty();
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

    public void incNodesEnabled(int shift) {
        nodesEnabled += shift;
    }

    public void decNodesEnabled(int shift) {
        nodesEnabled -= shift;
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

    public int getNodesEnabled() {
        return nodesEnabled;
    }

    public void setNodesEnabled(int nodesEnabled) {
        this.nodesEnabled = nodesEnabled;
    }

    public GraphModel getGraphModel() {
        return dhns;
    }
}
