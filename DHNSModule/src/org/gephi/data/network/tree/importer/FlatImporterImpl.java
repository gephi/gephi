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
import org.gephi.data.network.api.FlatImporter;
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
public class FlatImporterImpl implements FlatImporter {

    private int currentPre = 0;
    private Dhns dhns;
    private TreeStructure treeStructure;
    private PreNode root;
    private SightImpl sight;

    //Stats
    private int nodeImported = 0;
    private int edgeImported = 0;

    public FlatImporterImpl(Dhns dhns) {
        this.dhns = dhns;
        this.treeStructure = dhns.getTreeStructure();
        this.sight = dhns.getSightManager().getMainSight();
    }

    public void initImport() {
        dhns.getWriteLock().lock();
        root = new PreNode(0, 0, 0, null);
        treeStructure.insertAtEnd(root);
        treeStructure.setRoot(root);
        currentPre++;
        treeStructure.treeHeight = 1;
    }

    public void addNode(Node node) {
        PreNode p = new PreNode(currentPre, 0, 0, root);
        p.getPost();
        p.addSight(sight);
        p.setEnabled(sight, true);

        //Node
        NodeImpl nodeImpl = (NodeImpl) node;
        p.setNode(nodeImpl);

        //Insert
        treeStructure.insertAtEnd(p);
        currentPre++;

        nodeImported++;

        dhns.getDictionary().addNode(p);        //Dico
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

    public void finishImport() {
        //root
        root.getPost();
        root.addSight(sight);
        root.setEnabled(sight, false);

        dhns.getWriteLock().unlock();
        dhns.endImport();

        System.out.println(nodeImported+" nodes and "+edgeImported+" edges imported.");
    }
}
