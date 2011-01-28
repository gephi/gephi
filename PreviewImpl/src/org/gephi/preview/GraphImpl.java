/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
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
package org.gephi.preview;

import java.util.ArrayList;
import org.gephi.preview.api.BidirectionalEdge;
import org.gephi.preview.api.Graph;
import org.gephi.preview.api.Node;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.SelfLoop;
import org.gephi.preview.api.UndirectedEdge;
import org.gephi.preview.api.UnidirectionalEdge;

/**
 * Implementation of a preview graph.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class GraphImpl implements Graph {

    public static final float WEIGHT_MINIMUM = 0.4f;
    public static final float WEIGHT_MAXIMUM = 8f;
    private final PreviewModel model;
    private final ArrayList<Node> nodes = new ArrayList<Node>();
    private final ArrayList<SelfLoop> selfLoops = new ArrayList<SelfLoop>();
    private final ArrayList<UnidirectionalEdge> uniEdges = new ArrayList<UnidirectionalEdge>();
    private final ArrayList<BidirectionalEdge> biEdges = new ArrayList<BidirectionalEdge>();
    private final ArrayList<UndirectedEdge> undirectedEdges = new ArrayList<UndirectedEdge>();
    private float minWeight;
    private float maxWeight;
    private float minMetaWeight;
    private float maxMetaWeight;

    public GraphImpl(PreviewModel model) {
        this.model = model;
    }

    public Iterable<Node> getNodes() {
        return nodes;
    }

    public Iterable<SelfLoop> getSelfLoops() {
        return selfLoops;
    }

    public Iterable<UnidirectionalEdge> getUnidirectionalEdges() {
        return uniEdges;
    }

    public Iterable<BidirectionalEdge> getBidirectionalEdges() {
        return biEdges;
    }

    public Iterable<UndirectedEdge> getUndirectedEdges() {
        return undirectedEdges;
    }

    public int countNodes() {
        return nodes.size();
    }

    public int countSelfLoops() {
        return selfLoops.size();
    }

    public int countUnidirectionalEdges() {
        return uniEdges.size();
    }

    public int countBidirectionalEdges() {
        return biEdges.size();
    }

    public int countUndirectedEdges() {
        return undirectedEdges.size();
    }

    /**
     * Adds the given node to the graph.
     *
     * @param node  the node to add to the graph
     */
    public void addNode(NodeImpl node) {
        nodes.add(node);
    }

    /**
     * Adds the given self-loop to the graph.
     *
     * @param selfLoop  the self-loop to add to the graph
     */
    public void addSelfLoop(SelfLoop selfLoop) {
        selfLoops.add(selfLoop);
    }

    /**
     * Adds the given unidirectional edge to the graph.
     *
     * @param edge  the unidirectional edge to add to the graph
     */
    public void addUnidirectionalEdge(UnidirectionalEdge edge) {
        uniEdges.add(edge);
    }

    /**
     * Adds the given bidirectional edge to the graph.
     *
     * @param edge  the bidirectional edge to add to the graph
     */
    public void addBidirectionalEdge(BidirectionalEdge edge) {
        biEdges.add(edge);
    }

    /**
     * Adds the given undirected edge to the graph.
     *
     * @param edge  the undirected edge to add to the graph
     */
    public void addUndirectedEdge(UndirectedEdge edge) {
        undirectedEdges.add(edge);
    }

    public Boolean showNodes() {
        return model.getNodeSupervisor().getShowNodes();
    }

    public Boolean showEdges() {
        return model.getGlobalEdgeSupervisor().getShowFlag();
    }

    public Boolean showSelfLoops() {
        return model.getSelfLoopSupervisor().getShowFlag();
    }

    public PreviewModel getModel() {
        return model;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(float maxWeight) {
        this.maxWeight = maxWeight;
    }

    public float getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(float minWeight) {
        this.minWeight = minWeight;
    }

    public float getMinMetaWeight() {
        return minMetaWeight;
    }

    public void setMinMetaWeight(float minMetaWeight) {
        this.minMetaWeight = minMetaWeight;
    }

    public float getMaxMetaWeight() {
        return maxMetaWeight;
    }

    public void setMaxMetaWeight(float maxMetaWeight) {
        this.maxMetaWeight = maxMetaWeight;
    }
}
