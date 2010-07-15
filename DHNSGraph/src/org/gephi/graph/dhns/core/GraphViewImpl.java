/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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

    public Dhns getGraphModel() {
        return dhns;
    }
}
