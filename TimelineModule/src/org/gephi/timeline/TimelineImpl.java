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

import org.gephi.graph.api.Graph;
import org.gephi.timeline.api.Timeline;

/**
 *
 * @author Julian Bilcke
 */
public class TimelineImpl implements Timeline {

    private Graph graph;
    private TimelinePlayerImpl player;

    public String getName() {
        return "Desktop Timeline Controller";
    }
    
    public void setGraph(Graph graph) {
        //this.graph = new DirectedGraph();
        this.player = new TimelinePlayerImpl();
    }

    public void pause() {
        player.pause();
    }

    public void play() {
        player.play();
    }

    public void setPositionTo(double position) {
        player.setPositionTo(position);
    }

    public void setPositionTo(Comparable position) {
        player.setPositionTo(position);
    }

    public double getPosition() {
        return player.getPosition();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public boolean isStopped() {
        return player.isStopped();
    }

    public void setSpeed(double slicepersecond) {
        player.setSpeed(slicepersecond);
    }

    public Comparable getFirstComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Comparable getLastComparable() {
        throw new UnsupportedOperationException("Not supported yet.");
    }



}
