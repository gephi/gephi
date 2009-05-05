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
import org.gephi.data.network.Dhns;
import org.gephi.graph.api.EdgeWrap;
import org.gephi.graph.api.NodeWrap;
import org.gephi.data.network.api.SyncReader;

/**
 *
 * @author Mathieu Bastian
 */
public class SyncReaderImpl implements SyncReader {

    private Dhns dhns;
    private boolean locked = false;

    public SyncReaderImpl(Dhns dhns) {
        this.dhns = dhns;
    }

    public void lock() {
        if (!locked) {
            dhns.getReadLock().lock();
            locked = true;
        }
    }

    public void unlock() {
        if (locked) {
            dhns.getReadLock().unlock();
            locked = false;
        }
    }

    public Iterator<? extends NodeWrap> getNodes() {
        return dhns.getNetworkCache().getCacheContent().getNodeCache().iterator();
    }

    public Iterator<? extends EdgeWrap> getEdges() {
        return dhns.getNetworkCache().getCacheContent().getEdgeCache().iterator();
    }

    public int getNodeCount()
    {
        return dhns.getNetworkCache().getCacheContent().getNodeCache().size();
    }

    public int getEdgeCount()
    {
        return dhns.getNetworkCache().getCacheContent().getEdgeCache().size();
    }
}
