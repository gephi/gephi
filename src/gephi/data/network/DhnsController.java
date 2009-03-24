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

package gephi.data.network;

import gephi.data.network.sight.Sight;
import gephi.data.network.sight.SightManager;
import gephi.data.network.tree.importer.CompleteTreeImporter;
import gephi.visualization.VizController;

/**
 *
 * @author Mathieu
 */
public class DhnsController {

    private static DhnsController instance;

	private DhnsController()
	{ }

	public synchronized static DhnsController getInstance()
	{
		if(instance == null)
		{
			instance = new DhnsController();
		}
		return instance;
	}

    //Architecture
    private Dhns dhns;
    private TreeStructure treeStructure;
    private Sight mainSight;
    private SightManager sightManager;

    public void initInstances()
    {
        sightManager = new SightManager();
        dhns = new Dhns();
        treeStructure = dhns.getTreeStructure();
        mainSight = sightManager.createSight();

        importFakeGraph();

        dhns.init(mainSight);
    }

    private void importFakeGraph()
    {
        CompleteTreeImporter importer = new CompleteTreeImporter(treeStructure,mainSight);

		importer.importGraph(10, true);
		importer.shuffleEnable();
        System.out.println("Tree size : "+treeStructure.getTreeSize());
    }

    public Sight getMainSight() {
        return mainSight;
    }

    public TreeStructure getTreeStructure() {
        return treeStructure;
    }

    public Dhns getDhns() {
        return dhns;
    }
}
