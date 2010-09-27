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
package org.gephi.io.importer.impl;

import org.gephi.io.importer.api.EdgeDefault;

/**
 *
 * @author Mathieu Bastian
 */
public class ImportContainerParameters {

    private boolean selfLoops = true;
    private boolean parallelEdges = true;
    private boolean autoNode = true;
    private boolean autoScale = true;
    private boolean removeEdgeWithWeightZero = false;
    private boolean undirectedSumDirectedEdgesWeight = false;
    private boolean removeIntervalsOverlapping = true;
    private boolean mergeParallelEdgesWeight = true;
    private EdgeDefault edgeDefault = EdgeDefault.DIRECTED;

    public boolean isAutoNode() {
        return autoNode;
    }

    public void setAutoNode(boolean autoNode) {
        this.autoNode = autoNode;
    }

    public boolean isParallelEdges() {
        return parallelEdges;
    }

    public void setParallelEdges(boolean parallelEdges) {
        this.parallelEdges = parallelEdges;
    }

    public boolean isSelfLoops() {
        return selfLoops;
    }

    public void setSelfLoops(boolean selfLoops) {
        this.selfLoops = selfLoops;
    }

    public EdgeDefault getEdgeDefault() {
        return edgeDefault;
    }

    public void setEdgeDefault(EdgeDefault edgeDefault) {
        this.edgeDefault = edgeDefault;
    }

    public boolean isAutoScale() {
        return autoScale;
    }

    public void setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
    }

    public boolean isRemoveEdgeWithWeightZero() {
        return removeEdgeWithWeightZero;
    }

    public void setRemoveEdgeWithWeightZero(boolean removeEdgeWithWeightZero) {
        this.removeEdgeWithWeightZero = removeEdgeWithWeightZero;
    }

    public boolean isUndirectedSumDirectedEdgesWeight() {
        return undirectedSumDirectedEdgesWeight;
    }

    public void setUndirectedSumDirectedEdgesWeight(boolean undirectedSumDirectedEdgesWeight) {
        this.undirectedSumDirectedEdgesWeight = undirectedSumDirectedEdgesWeight;
    }

    public boolean isRemoveIntervalsOverlapping() {
        return removeIntervalsOverlapping;
    }

    public void setRemoveIntervalsOverlapping(boolean removeIntervalsOverlapping) {
        this.removeIntervalsOverlapping = removeIntervalsOverlapping;
    }

    public boolean isMergeParallelEdgesWeight() {
        return mergeParallelEdgesWeight;
    }

    public void setMergeParallelEdgesWeight(boolean mergeParallelEdgesWeight) {
        this.mergeParallelEdgesWeight = mergeParallelEdgesWeight;
    }
}
