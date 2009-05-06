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

import java.util.ArrayList;
import org.gephi.data.network.Dhns;
import org.gephi.data.network.api.HierarchyReader;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.ChildrenIterator;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyReaderImpl implements HierarchyReader {

    private Dhns dhns;

    public HierarchyReaderImpl(Dhns dhns) {
        this.dhns = dhns;
    }

    public Node[] getTopNodes() {
        ArrayList<Node> topList = new ArrayList<Node>();
        if(dhns.getTreeStructure().getTreeSize() >0)
        {
            ChildrenIterator itr = new ChildrenIterator(dhns.getTreeStructure());
            for(itr.setNode(dhns.getTreeStructure().getRoot());itr.hasNext();)
            {
                PreNode preNode = itr.next();
                topList.add(preNode.getNode());
            }
            return topList.toArray(new Node[0]);
        }
        return new Node[0];
    }

    public boolean hasChildren(Node node) {
        return !((NodeImpl)node).getPreNode().isLeaf();
    }

    public Node[] getChildren(Node node) {
        ArrayList<Node> childList = new ArrayList<Node>();
        ChildrenIterator itr = new ChildrenIterator(dhns.getTreeStructure());
        for(itr.setNode(((NodeImpl)node).getPreNode());itr.hasNext();)
        {
            PreNode preNode = itr.next();
            childList.add(preNode.getNode());
        }
        return childList.toArray(new Node[0]);
    }

    public void lock() {
        dhns.getReadLock().lock();
    }

    public void unlock() {
        dhns.getReadLock().unlock();
    }
}
