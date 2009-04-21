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
package org.gephi.data.network.potato;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.Dhns;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.tree.TreeStructure;

/**
 * @author Mathieu Bastian
 */
public class PotatoCooker {

    //Use
    private List<PreNode> contextNodes;
    private TreeStructure treeStructure;

    //Algo
    private PreNode nextContextNode;
    private int currentContextIndex = 1;
    private int currentPre;
    private int currentPost;
    private int nextPre = 0;
    private int nextPost = 0;
    private PreNode pointer;

    //Result
    private List<PotatoImpl> potatoes;

    public PotatoCooker(Dhns dhns) {
        this.treeStructure = dhns.getTreeStructure();
    }

    public void cookPotatoes(List<PreNode> enabledNodes) {
        potatoes = new ArrayList<PotatoImpl>();

        //Condition
        if (!enabledNodes.isEmpty()) {
            //Init
            contextNodes = enabledNodes;
            initAncestorAxisWalk();

            //Fill potatoes
            ancestorAxisWalk();

            //Result
            orderResults();
        }
    }

    private void initAncestorAxisWalk() {

        currentContextIndex = contextNodes.size() - 1;
        pointer = nextContextNode();
        currentPre = pointer.parent.pre;
        currentPost = pointer.post;
        if (hasNextContextNode()) {
            nextContextNode = nextContextNode();
            nextPre = nextContextNode.pre;
            nextPost = nextContextNode.post;
        }
    }

    //Use ancestor axis techniques
    private void ancestorAxisWalk() {
        PotatoImpl potato = new PotatoImpl();
        potato.addContent(pointer);

        while (currentContextIndex >= 0 || currentPre > 0) {
            while (currentPre < nextPre) {
                if (nextContextNode != null) {
                    potato.addContent(nextContextNode);
                }

                if (nextPost < currentPost) {
                    currentPre = nextContextNode.parent.pre;
                    currentPost = nextPost;
                }

                if (hasNextContextNode()) {
                    nextContextNode = nextContextNode();
                    nextPre = nextContextNode.pre;
                    nextPost = nextContextNode.post;
                } else {
                    //No more context nodes
                    nextPre = 0;
                    nextContextNode = null;
                }

            }

            pointer = treeStructure.getNodeAt(currentPre);
            currentPre = pointer.parent.pre;

            potato.setNode(pointer);
            potatoes.add(potato);

            //Refresh
            potato = new PotatoImpl();
        }
    }

    private void orderResults() {
    }

    private PreNode nextContextNode() {
        return contextNodes.get(currentContextIndex);
    }

    private boolean hasNextContextNode() {
        currentContextIndex--;
        if (currentContextIndex >= 0) {
            return true;
        }
        return false;
    }

    public List<PotatoImpl> getPotatoes() {
        return potatoes;
    }
}
