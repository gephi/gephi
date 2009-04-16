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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.datastructure.avl.simple.AVLItem;
import org.gephi.datastructure.avl.simple.SimpleAVLTree;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class SightsModel implements Lookup.Provider {

    private SimpleAVLTree sightTree = new SimpleAVLTree();
    private SightImpl mainSight;
    private SightImpl selectedSight;
    private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    //Lookup
    private transient InstanceContent ic;
    private transient AbstractLookup lookup;

    public SightsModel() {
        ic = new InstanceContent();
        lookup = new AbstractLookup(ic);
    }

    public SightImpl getMainSight() {
        return mainSight;
    }

    public void setMainSight(SightImpl mainSight) {
        this.mainSight = mainSight;
    }

    public void setSelectedSight(SightImpl selectedSight) {
        if (selectedSight != this.selectedSight) {
            this.selectedSight = selectedSight;
            fireChangeEvent();
        }
    }

    public SightImpl getSelectedSight() {
        return selectedSight;
    }

    public void addSight(SightImpl sight) {
        sightTree.add(sight);
        ic.add(sight);
    }

    public void removeSight(SightImpl sight) {
        if (sightTree.remove(sight)) {
            ic.remove(sight);
        }
    }

    public void removeSight(int number) {
        if (sightTree.remove(number)) {
            ic.remove(ic);
        }
    }

    public SightImpl getSight(int number) {
        return (SightImpl) sightTree.get(number);
    }

    public Iterator<AVLItem> getSightIterator() {
        return sightTree.iterator();
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
}
