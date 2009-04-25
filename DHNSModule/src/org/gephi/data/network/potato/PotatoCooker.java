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
import java.util.Iterator;
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
    private PotatoManager potatoManager;

    //Result
    private List<PotatoImpl> potatoes;

    //Config
    private int levelLimit=1;

    public PotatoCooker(Dhns dhns, PotatoManager manager) {
        this.treeStructure = dhns.getTreeStructure();
        this.potatoManager = manager;
    }

    public void cookPotatoes(List<PreNode> enabledNodes) {


        //Condition
        if (!enabledNodes.isEmpty()) {
            //Init
            contextNodes = enabledNodes;
            resetPotatoes();
            potatoes = new ArrayList<PotatoImpl>();

            //Fill potatoes
            createPotatoes();

            //Result
            orderResults();
        }
    }

    private void resetPotatoes() {
        if (potatoes == null) {
            return;
        }

        for (int i = 0; i < potatoes.size(); i++) {
            PotatoImpl potato = potatoes.get(i);
            potato.getNode().getPreNode().setPotato(null);
        }
    }

    private void createPotatoes() {

        PreNode currentParent = null;
        PotatoImpl currentPotato = null;

        for (int i = 0; i < contextNodes.size(); i++) {
            PreNode node = contextNodes.get(i);
            PreNode parent = node.parent;

            if (parent != currentParent) {
                currentParent = parent;

                if (parent.getPotato() != null) {
                    currentPotato = parent.getPotato();
                } else {
                    //New Potato
                    currentPotato = new PotatoImpl(potatoManager);
                    currentPotato.setNode(parent);
                    potatoes.add(currentPotato);
                }

            }
            currentPotato.addContent(node);
        }
    }

    private void orderResults() {
        //Delete empty
        /*for (Iterator<PotatoImpl> itr = potatoes.iterator(); itr.hasNext();) {
        PotatoImpl p = itr.next();
        if (p.countContent() == 0) {
        itr.remove();
        }
        }*/

        //Create potatoes hierarchy
        int size = potatoes.size();
        for (int i = 0; i < size; i++) {
            PotatoImpl potato = potatoes.get(i);
            PreNode node = potato.getNode().getPreNode();
            int level=1;
            while (node.parent != null && node.parent.parent!=null && level <= levelLimit) {
                PreNode parent = node.parent;
                PotatoImpl parentPotato = parent.getPotato();
                if (parentPotato == null) {
                    parentPotato = new PotatoImpl(potatoManager);
                    parentPotato.setNode(parent);
                    parentPotato.setLevel(level);
                    potatoes.add(parentPotato);
                }
                parentPotato.addChild(potato);
                potato.setFather(parentPotato);

                potato = parentPotato;
                node = parent;
                level++;
            }
        }

        for (int i = 0; i < potatoes.size(); i++) {
            PotatoImpl potato = potatoes.get(i);
            potato.fillContent();
        }
    }

    public List<PotatoImpl> getPotatoes() {
        return potatoes;
    }
}
