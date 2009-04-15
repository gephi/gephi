/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.data.network.sight;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.node.PreNode;

/**
 *
 * @author Mathieu Bastian
 */
public class SightCacheContent {

    List<PreNode> nodeCache;
    List<DhnsEdge> edgeCache;
    int nodeVersion = 0;
    int edgeVersion = 0;

    public SightCacheContent() {
        this.nodeCache = new ArrayList<PreNode>();
        this.edgeCache = new ArrayList<DhnsEdge>();
    }

    public SightCacheContent(SightCacheContent instance) {
        this.nodeCache = instance.nodeCache;
        this.edgeCache = instance.edgeCache;
        this.nodeVersion = instance.nodeVersion;
        this.edgeVersion = instance.edgeVersion;
    }

    public SightCacheContent(List<PreNode> nodeCache, List<DhnsEdge> edgeCache) {
        this.nodeCache = nodeCache;
        this.edgeCache = edgeCache;
    }

    public void appendContent(SightCacheContent newContent) {
        if (newContent.nodeCache != null) {
            this.nodeCache = newContent.nodeCache;
            this.nodeVersion++;
        }

        if (newContent.edgeCache != null) {
            this.edgeCache = newContent.edgeCache;
            this.edgeVersion++;
        }
    }

    public List<DhnsEdge> getEdgeCache() {
        return edgeCache;
    }

    public List<PreNode> getNodeCache() {
        return nodeCache;
    }

    public int getNodeVersion() {
        return nodeVersion;
    }

    public int getEdgeVersion() {
        return edgeVersion;
    }
}
