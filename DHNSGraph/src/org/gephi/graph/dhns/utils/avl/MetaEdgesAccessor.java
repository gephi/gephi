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
package org.gephi.graph.dhns.utils.avl;

import org.gephi.datastructure.avl.param.AVLItemAccessor;
import org.gephi.datastructure.avl.param.ParamAVLTree;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.view.View;

/**
 *
 * @author Mathieu Bastian
 */
public class MetaEdgesAccessor {

    private static ViewAVLAccessor viewAVLAccessor = new ViewAVLAccessor();
    private ViewMetaEdgeTree inTree;
    private ViewMetaEdgeTree outTree;

    public MetaEdgesAccessor() {
        inTree = new ViewMetaEdgeTree();
        outTree = new ViewMetaEdgeTree();
    }

    public MetaEdgeTree getMetaEdgeInTree(View view) {
        return inTree.getItem(view.getNumber());
    }

    public MetaEdgeTree getMetaEdgeOutTree(View view) {
        return outTree.getItem(view.getNumber());
    }

    public void createMetaEdgeTree(AbstractNode owner, View view) {
        inTree.add(new MetaEdgeTree(owner, view));
        outTree.add(new MetaEdgeTree(owner, view));
    }

    public void removeMetaEdgeTree(View view) {
        inTree.remove(inTree.getItem(view.getNumber()));
    }

    private static class ViewMetaEdgeTree extends ParamAVLTree<MetaEdgeTree> {

        public ViewMetaEdgeTree() {
            super(viewAVLAccessor);
        }
    }

    private static class ViewAVLAccessor implements AVLItemAccessor<MetaEdgeTree> {

        public int getNumber(MetaEdgeTree item) {
            return item.getView().getNumber();
        }
    }
}
