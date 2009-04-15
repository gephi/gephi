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
package org.gephi.data.network.reader;

import java.util.Iterator;
import org.gephi.graph.api.EdgeWrap;
import org.gephi.graph.api.NodeWrap;
import org.gephi.data.network.api.AsyncReader;
import org.gephi.data.network.sight.SightCacheContent;
import org.gephi.data.network.sight.SightImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class AsyncReaderImpl implements AsyncReader {

    private SightImpl sight;

    //Versionning
    private SightCacheContent cacheContent;
    private int currentNodeVersion = -1;
    private int currentEdgeVersion = -1;

    public AsyncReaderImpl(SightImpl sight) {
        this.sight = sight;
    }

    public Iterator<? extends NodeWrap> getNodes() {
        currentNodeVersion = cacheContent.getNodeVersion();
        return cacheContent.getNodeCache().iterator();
    }

    public Iterator<? extends EdgeWrap> getEdges() {
        currentEdgeVersion = cacheContent.getEdgeVersion();
        return cacheContent.getEdgeCache().iterator();
    }

    public boolean requireUpdate() {
        cacheContent = sight.getSightCache().getCacheContent();
        if (cacheContent.getNodeVersion() > currentNodeVersion || cacheContent.getEdgeVersion() > currentEdgeVersion) {
            return true;
        }
        return false;
    }

    public boolean requireNodeUpdate() {
        if (cacheContent.getNodeVersion() > currentNodeVersion) {
            return true;
        }
        return false;
    }

    public boolean requireEdgeUpdate() {
        if (cacheContent.getEdgeVersion() > currentEdgeVersion) {
            return true;
        }
        return false;
    }
}
