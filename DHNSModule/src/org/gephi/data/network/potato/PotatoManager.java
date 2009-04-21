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
import org.gephi.data.network.node.treelist.SingleTreeIterator;
import org.gephi.data.network.sight.SightImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class PotatoManager {

    private PotatoCooker cooker;
    private PotatoRender render;
    private Dhns dhns;

    public PotatoManager(Dhns dhns) {
        this.dhns = dhns;
        render = new PotatoRender();
        cooker = new PotatoCooker(dhns, this);
    }

    public List<PotatoImpl> cookPotatoes(SightImpl sight) {
        List<PreNode> enabledNodes = new ArrayList<PreNode>();
        SingleTreeIterator itr = new SingleTreeIterator(dhns.getTreeStructure(), sight);
        for (; itr.hasNext();) {
            PreNode enabledNode = itr.next();
            enabledNodes.add(enabledNode);
        }

        cooker.cookPotatoes(enabledNodes);
        List<PotatoImpl> result = cooker.getPotatoes();

        return result;
    }

    public void renderPotato(PotatoImpl potato) {
        render.renderPotato(potato);
    }
}
