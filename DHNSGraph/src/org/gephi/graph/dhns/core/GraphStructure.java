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
package org.gephi.graph.dhns.core;

import org.gephi.graph.dhns.utils.avl.AbstractEdgeTree;
import org.gephi.graph.dhns.utils.avl.AbstractNodeTree;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphStructure {

    private TreeStructure treeStructure;
    private AbstractNodeTree nodeDictionnary;
    private AbstractEdgeTree edgeDictionnary;

    public GraphStructure() {
        treeStructure = new TreeStructure(this);
        nodeDictionnary = new AbstractNodeTree();
        edgeDictionnary = new AbstractEdgeTree();
    }

    public TreeStructure getStructure() {
        return treeStructure;
    }

    public AbstractNodeTree getNodeDictionnary() {
        return nodeDictionnary;
    }

    public AbstractEdgeTree getEdgeDictionnary() {
        return edgeDictionnary;
    }
}
