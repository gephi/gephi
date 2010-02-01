/*
Copyright 2010 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
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
package org.gephi.timeline.api;

import java.util.List;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Julian Bilcke <julian.bilcke@gmail.com>
 */
public interface TimelineModel {

    /** get the data array
     *
     * used by the Data Layer
     *
     */
    public List<Float> getOverviewSample(int resolution);

    public List<Float> getZoomSample(int resolution);

    public void setInterval(Float from, Float to);

    public void setTo(Float to);

    public void setFrom(Float from);

    public Float getFromAsFloat();

    public Float getToAsFloat();

    /**
     * The first comparable, ie. the <code>Comparable</code> attribute attached to the oldest entry
     *
     * @return a comparable
     */
    public Comparable getFirstComparable();

    /**
     * The last comparable, ie. the <code>Comparable</code> attribute attached to the youngest entry
     *
     * @return a comparable
     */
    public Comparable getLastComparable();

    public Comparable getSelectionFromAsComparable();

    public Comparable getSelectionToAsComparable();

    /**
     * Play the timeline using the current playmode
     *
     * @return a comparable
     */
    public void play();

    /**
     * Play the timeline using another playmode. Will stop current playmode if
     * necessary, and start the new one.
     *
     * @return a comparable
     */
    public void play(TimelinePlayMode playMode);

    /**
     * Switch between pause and play mode.
     *
     * @return the new pause status
     */
    public boolean togglePause();

    /**
     * Set the new timeline's pause status
     *
     * @return void
     */
    public void setPause(boolean p);

    /**
     * stop the timeline
     *
     * @return void
     */
    public void stop();

    /**
     * check is the timeline is paused
     *
     * @return true if the timeline is stopped
     */
    public boolean isPaused();

    /**
     * check is the timeline is stopped
     *
     * @return true is the timeline is stopped
     */
    public boolean isStopped();

    /**
     * set the timeline's play mode
     *
     * @return
     */
    public void setTimelinePlayMode(TimelinePlayMode playMode);

    /**
     * get the timeline's play mode
     *
     * @return TimelinePlayMode
     */
    public TimelinePlayMode getTimelinePlayMode();

    /**
     * set the timeline's step by tick
     *
     * @return
     */
     public void setStepByTick(float s);

    /**
     * get the timeline step by tick
     *
     * @return a float corresponding to the step by tick's value
     */
    public float getStepByTick();


    public void addChangeListener(ChangeListener changeListener);

    public void removeChangeListener(ChangeListener changeListener);
}
