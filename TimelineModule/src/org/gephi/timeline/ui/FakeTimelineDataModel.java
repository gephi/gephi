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
package org.gephi.timeline.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.gephi.timeline.api.TimelineDataModel;
import org.gephi.timeline.api.TimelineDataModelListener;

/**
 *
 * @author Julian Bilcke
 */
public class FakeTimelineDataModel implements TimelineDataModel {

    private Random random;
    private List<Float> data;
    private List<TimelineDataModelListener> listeners;

    // FAKE DATASET SIZE
    private int NB_OF_FAKE_REVISIONS = 80000;

    public FakeTimelineDataModel() {
        listeners = new ArrayList<TimelineDataModelListener>();

        // WE GENERATE OUR FAKE DATASET
        random = new Random();
        data = new ArrayList<Float>();
        for (int i = 0; i < NB_OF_FAKE_REVISIONS; i++) {
            data.add(0.15f + random.nextFloat() * 0.6f);
        }
    }

    public List<Float> getDataSample(int resolution) {
        // TODO put a call to the timeline engine here ?

        List<Float> tmp = new ArrayList<Float>();
        
        int unit = NB_OF_FAKE_REVISIONS / resolution; // eg.  16 = 10000 / 600
        for (int i = 0; i < resolution; i++) {
            tmp.add(data.get(i * unit)); // eg. 600 * chunks of 16
        }
        return tmp;
    }

    public void addTimelineDataModelListener(TimelineDataModelListener listener) {
        listeners.add(listener);
    }
}
