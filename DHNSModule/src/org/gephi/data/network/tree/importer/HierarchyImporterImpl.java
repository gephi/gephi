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

package org.gephi.data.network.tree.importer;

import org.gephi.data.network.Dhns;
import org.gephi.data.network.api.HierarchyImporter;
import org.gephi.data.network.edge.EdgeImpl;
import org.gephi.data.network.edge.PreEdge;
import org.gephi.data.network.edge.PreEdge.EdgeType;
import org.gephi.data.network.node.NodeImpl;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public class HierarchyImporterImpl implements HierarchyImporter {

    private Dhns dhns;
    private TreeStructure treeStructure;
    private SightImpl sight;

    //Tree
    private int currentLevel;
    private PreNode currentParent;
    private int currentSize;
    private PreNode lastPos;
    private int treeHeight;
    private int currentPre = 0;

    //Stats
    private int nodeImported = 0;
    private int edgeImported = 0;

    public HierarchyImporterImpl(Dhns dhns) {
        this.dhns = dhns;
        this.treeStructure = dhns.getTreeStructure();
        this.sight = dhns.getSightManager().getMainSight();
    }

    /**
     * Create the (virtual) root of the tree and prepare import.
     */
    public void initImport() {
        dhns.getWriteLock().lock();
        PreNode root = new PreNode(0, 0, 0, null);
        root.addSight(sight);
        treeStructure.insertAtEnd(root);
        treeStructure.setRoot(root);
        currentLevel = 1;
        currentParent = root;
        currentSize = 0;
        lastPos = root;
        treeHeight = 0;
    }

    /**
     * Go down into the tree and increase the level. Next <code>addSibling()</code> will be a child
     * of the current node.
     */
    public void addChild() {
        currentParent.size = currentSize;
        currentLevel++;
        currentParent = lastPos;
        currentSize = 0;
        treeHeight = Math.max(treeHeight, currentLevel);
    }

    /**
     * Add a new node to the current parent. Create the {@link PreNode} object.
     */
    public void addSibling(Node node) {
        PreNode p = new PreNode(currentPre, 0, currentLevel, currentParent);

        //Node
        NodeImpl nodeImpl = (NodeImpl) node;
        p.setNode(nodeImpl);
        p.addSight(sight);
        
        //Insert
        treeStructure.insertAtEnd(p);
        currentSize++;
        lastPos = p;
        currentPre++;

        dhns.getDictionary().addNode(p);        //Dico
        nodeImported++;
    }

    /**
     * Go up into the tree and decrease the current level.
     */
    public void closeChild() {
        PreNode parent = currentParent;
        parent.size = currentSize;
        if (parent.parent != null) {
            PreNode parentParent = parent.parent;
            parentParent.size += currentSize;
            currentSize = parentParent.size;
            currentParent = parent.parent;
        }

        currentLevel--;
    }

    /**
     * Finish import.
     */
    public void finishImport() {
        closeChild();
        treeStructure.treeHeight = treeHeight;

         for (PreNode p : treeStructure.getTree()) {
            p.getPost();
            
            if (p.size == 0) {
                p.setEnabled(sight, true);
            }
        }

        dhns.getWriteLock().unlock();
        dhns.endImport();

        System.out.println(nodeImported+" nodes and "+edgeImported+" edges imported.");
    }

    public void addEdge(Edge edge) {
        EdgeImpl edgeImpl = (EdgeImpl) edge;
        NodeImpl source = edgeImpl.getSource();
        NodeImpl target = edgeImpl.getTarget();

        PreNode preNodeSource = source.getPreNode();
        PreNode preNodeTarget = target.getPreNode();

        PreEdge preEdge=null;
        if (preNodeSource.getPre() < preNodeTarget.getPre()) {
            //Edge out to target node
            preEdge = new PreEdge(EdgeType.OUT, preNodeSource, preNodeTarget);
            preNodeSource.addForwardEdge(preEdge);
            preNodeTarget.addBackwardEdge(preEdge);
        } else {
            //Edge in to source node
            preEdge = new PreEdge(EdgeType.IN, preNodeTarget, preNodeSource);
            preNodeSource.addForwardEdge(preEdge);
            preNodeTarget.addBackwardEdge(preEdge);
        }
        preEdge.setEdge(edge);

        edgeImported++;

        dhns.getDictionary().addEdge(preEdge);         //Dico
    }
}
