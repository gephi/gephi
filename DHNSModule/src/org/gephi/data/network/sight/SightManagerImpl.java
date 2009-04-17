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

import javax.swing.event.ChangeListener;
import org.gephi.data.network.Dhns;
import org.gephi.data.network.api.SightManager;
import org.gephi.graph.api.Sight;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 *
 * @author Mathieu Bastian
 */
public class SightManagerImpl implements SightManager {

    private int sightCounter = 1;
    private SightsModel model;
    
    private Dhns dhns;

    public SightManagerImpl(Dhns dhns) {
        this.dhns = dhns;
        this.model = new SightsModel();

        //Create Main Sight
        SightImpl mainSight = createSight();
        model.setMainSight(mainSight);
        model.setSelectedSight(mainSight);
    }

    public SightImpl getMainSight() {
        return model.getMainSight();
    }

    public SightImpl createSight() {
        SightImpl sight = new SightImpl(getSightCounter());
        sight.setSightCache(new SightCache(dhns, sight));
        sightCounter++;
        model.addSight(sight);
        return sight;
    }

    public void addChangeListener(ChangeListener listener)
    {
        model.addChangeListener(WeakListeners.change(listener, model));

    }

    public Lookup getModelLookup() {
        return model.getLookup();
    }

    public SightImpl getSelectedSight() {
        return model.getSelectedSight();
    }

    public void selectSight(Sight sight) {
        model.setSelectedSight((SightImpl)sight);
    }

    public int getSightCounter() {
        return sightCounter;
    }

    public void setSightCounter(int sightCounter) {
        this.sightCounter = sightCounter;
    }

    public void updateSight(SightImpl sight) {
        sight.getSightCache().reset();
    }
}