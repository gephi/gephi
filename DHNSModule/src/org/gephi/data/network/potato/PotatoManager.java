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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
    private List<PotatoImpl> currentPotatoes;
    private ExecutorService potatoRenderExecutor;

    public PotatoManager(Dhns dhns) {
        this.dhns = dhns;
        render = new PotatoRender();
        cooker = new PotatoCooker(dhns, this);

        potatoRenderExecutor = Executors.newSingleThreadExecutor();
    }

    public List<PotatoImpl> cookPotatoes(SightImpl sight) {
        List<PreNode> enabledNodes = new ArrayList<PreNode>();
        SingleTreeIterator itr = new SingleTreeIterator(dhns.getTreeStructure(), sight);
        for (; itr.hasNext();) {
            PreNode enabledNode = itr.next();
            enabledNodes.add(enabledNode);
        }

        cooker.cookPotatoes(enabledNodes);
        currentPotatoes = cooker.getPotatoes();

        return currentPotatoes;
    }

    public void renderPotato(final PotatoImpl potato, final boolean propagateAncestors) {
        Future futur = potato.getDisplayTask();
        if (futur != null) {
            futur.cancel(false);
        }

        futur = potatoRenderExecutor.submit(new Runnable() {

            public void run() {
                render.renderPotato(potato);
                if (propagateAncestors) {
                    PotatoImpl currentFather = potato.getFather();
                    while (currentFather != null) {
                        render.renderPotato(currentFather);
                        currentFather = currentFather.getFather();
                    }
                }
            }
        });
        potato.setDisplayTask(futur);
    }

    public int getTreeHeight() {
        return dhns.getTreeStructure().treeHeight;
    }

    public PotatoRender getPotatoRender() {
        return render;
    }

    public void updatePotatoesRender() {
        if (currentPotatoes == null) {
            return;
        }

        for (PotatoImpl p : currentPotatoes) {
            p.updatePotatoHierarchy();
        }
    }

    public String getPotatoesSVG()
    {
        return render.computePolygon(currentPotatoes);
    }
}
