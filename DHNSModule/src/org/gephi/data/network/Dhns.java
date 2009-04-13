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
package org.gephi.data.network;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.network.api.FreeModifier;
import org.gephi.data.network.tree.TreeStructure;
import org.gephi.data.network.config.DHNSConfig;
import org.gephi.data.network.mode.FreeMode;
import org.gephi.data.network.node.PreNode;
import org.gephi.data.network.node.treelist.SingleTreeIterator;
import org.gephi.data.network.potato.Potato;
import org.gephi.data.network.potato.PotatoBuilder;
import org.gephi.data.network.potato.PotatoRender;
import org.gephi.data.network.sight.SightImpl;
import org.gephi.data.network.sight.SightManager;
import org.gephi.data.network.tree.importer.CompleteTreeImporter;
import org.gephi.data.network.utils.RandomEdgesGenerator;

public class Dhns {

    private DHNSConfig config;
    private TreeStructure treeStructure;
    private FreeMode freeMode;
    private SightManager sightManager;
    private PotatoBuilder potatoBuilder;

    public Dhns() {
        config = new DHNSConfig();
        treeStructure = new TreeStructure();
        sightManager = new SightManager(this);
        freeMode = new FreeMode(this);
    }

    public void init(SightImpl sight) {
        importFakeGraph();
        treeStructure.showTreeAsTable();
    }

    private void importFakeGraph() {
        CompleteTreeImporter importer = new CompleteTreeImporter(treeStructure, sightManager.getMainSight());

        importer.importGraph(2, true);
        //importer.shuffleEnable();
        System.out.println("Tree size : " + treeStructure.getTreeSize());
        treeStructure.showTreeAsTable();

        potatoBuilder = new PotatoBuilder(this);
        List<PreNode> enabledNodes = new ArrayList<PreNode>();
        SingleTreeIterator itr = new SingleTreeIterator(treeStructure, sightManager.getMainSight());
        for(;itr.hasNext();)
        {
            PreNode enabledNode = itr.next();
            enabledNodes.add(enabledNode);
            System.out.println("Enabled : "+enabledNode);
        }
        potatoBuilder.buildPotatoes(enabledNodes);
        PotatoRender render = new PotatoRender();
        for(Potato p : potatoBuilder.getPotatoes())
        {
            render.cookPotato(p);
        }

        /*RandomEdgesGenerator reg = new RandomEdgesGenerator(treeStructure);
        reg.generatPhysicalEdges(20);*/
        freeMode.init();
    }

    

    public TreeStructure getTreeStructure() {
        return treeStructure;
    }

    public SightManager getSightManager() {
        return sightManager;
    }

    public DHNSConfig getConfig() {
        return config;
    }

    public FreeModifier getFreeModifier() {
        return freeMode;
    }

    public PotatoBuilder getPotatoBuilder() {
        return potatoBuilder;
    }
}
