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
package org.gephi.desktop.clustering;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.clustering.api.ClusteringModel;
import org.gephi.clustering.spi.Clusterer;

/**
 *
 * @author Mathieu Bastian
 */
public class ClusteringModelImpl implements ClusteringModel {

    //Listeners
    private List<ChangeListener> listeners;
    //Architecture
    private Clusterer selectedClusterer;
    private List<Clusterer> clusterers;
    private boolean running = false;

    public ClusteringModelImpl() {
        clusterers = new ArrayList<Clusterer>();
        listeners = new ArrayList<ChangeListener>();
    }

    public Clusterer getSelectedClusterer() {
        return selectedClusterer;
    }

    public Clusterer[] getClusterers() {
        return clusterers.toArray(new Clusterer[0]);
    }

    public void addClusterer(Clusterer clusterer) {
        for (int i = 0; i < clusterers.size(); i++) {
            if (clusterers.get(i).getClass().equals(clusterer.getClass())) {
                clusterers.set(i, clusterer);
                return;
            }
        }
        clusterers.add(clusterer);
    }

    public void removeClusterer(Clusterer clusterer) {
        clusterers.remove(clusterer);
    }

    public void setSelectedClusterer(Clusterer selectedClusterer) {
        this.selectedClusterer = selectedClusterer;
        fireChangeEvent();
    }

    public void setRunning(boolean running) {
        this.running = running;
        fireChangeEvent();
    }

    public boolean isRunning() {
        return running;
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    private void fireChangeEvent() {
        ChangeEvent changeEvent = new ChangeEvent(this);
        for (ChangeListener changeListener : listeners) {
            changeListener.stateChanged(changeEvent);
        }
    }
}
