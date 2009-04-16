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
package org.gephi.data.network.sight;

import org.gephi.data.network.Dhns;
import org.gephi.project.api.Projects;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Mathieu Bastian
 */
public class SightManager {

    private int sightCounter = 1;
    private SightsModel model;
    private SightImpl mainSight;
    private Dhns dhns;

    public SightManager(Dhns dhns)
    {
        this.dhns = dhns;
        this.model = new SightsModel();
    }

    public SightImpl getMainSight()
    {
        if(mainSight==null)
            mainSight = createSight();
        return mainSight;
    }

    public SightImpl createSight() {
        SightImpl sight = new SightImpl(getSightCounter());
        sight.setSightCache(new SightCache(dhns, sight));
        sightCounter++;
        model.addSight(sight);
        return sight;
    }

   

    

    public int getSightCounter() {
        return sightCounter;
    }

    public void setSightCounter(int sightCounter) {
        this.sightCounter = sightCounter;
    }

    public void updateSight(SightImpl sight)
    {
        sight.getSightCache().reset();
    }
}