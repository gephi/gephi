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
package org.gephi.data.network.cache;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.edge.DhnsEdge;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.potato.PotatoImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class NetworkCacheContent {

    List<PreNode> nodeCache;
    List<DhnsEdge> edgeCache;
    List<PotatoImpl> potatoCache;
    int nodeVersion = 0;
    int edgeVersion = 0;
    int potatoVersion = 0;

    public NetworkCacheContent() {
        this.nodeCache = new ArrayList<PreNode>();
        this.edgeCache = new ArrayList<DhnsEdge>();
        this.potatoCache = new ArrayList<PotatoImpl>();
    }

    public NetworkCacheContent(NetworkCacheContent instance) {
        this.nodeCache = instance.nodeCache;
        this.edgeCache = instance.edgeCache;
        this.potatoCache = instance.potatoCache;
        this.nodeVersion = instance.nodeVersion;
        this.edgeVersion = instance.edgeVersion;
        this.potatoVersion = instance.potatoVersion;
    }

    public NetworkCacheContent(List<PreNode> nodes, List<DhnsEdge> edges, List<PotatoImpl> potatoes) {
        this.nodeCache = nodes;
        this.edgeCache = edges;
        this.potatoCache = potatoes;
    }

    public void appendContent(NetworkCacheContent newContent) {
        if (newContent.nodeCache != null) {
            this.nodeCache = newContent.nodeCache;
            this.nodeVersion++;
        }

        if (newContent.edgeCache != null) {
            this.edgeCache = newContent.edgeCache;
            this.edgeVersion++;
        }

        if (newContent.potatoCache != null) {
            this.potatoCache = newContent.potatoCache;
            this.potatoVersion++;
        }
    }

    public List<DhnsEdge> getEdgeCache() {
        return edgeCache;
    }

    public List<PreNode> getNodeCache() {
        return nodeCache;
    }

    public List<PotatoImpl> getPotatoCache() {
        return potatoCache;
    }

    public int getNodeVersion() {
        return nodeVersion;
    }

    public int getEdgeVersion() {
        return edgeVersion;
    }

    public int getPotatoVersion() {
        return potatoVersion;
    }
}
