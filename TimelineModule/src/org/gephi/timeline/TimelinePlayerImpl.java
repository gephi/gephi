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
package org.gephi.timeline;

import org.gephi.timeline.api.TimelineDataModel;
import org.gephi.timeline.api.TimelineQuartz;


/**
 *
 * @author Julian Bilcke
 */
public class TimelinePlayerImpl {

    private TimelineQuartz quartz;
    private Thread thread;
    private TimelineDataModel model;

    public TimelinePlayerImpl() {
        quartz = new TimelineQuartzImpl();
        model = null;
    }

    public void pause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void play() {
        thread.start();

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTimelineModel(TimelineDataModel model) {
        this.model = model;
    }

    public void setPositionTo(double position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPositionTo(Comparable position) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getPosition() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isPlaying() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isStopped() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSpeed(double slicepersecond) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
