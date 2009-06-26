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

package org.gephi.timeline.api;

/**
 * The Timeline Controller interface
 *
 * Provides a public interface to the timeline
 *
 * @author Julian Bilcke
 */
public interface TimelineController {

    /**
     * pause the timeline controller
     *
     */
    public void pause();

    /**
     * start the timeline controller
     *
     */
    public void play();

    /**
     * set the position of the timeline controller. must be a ratio.
     *
     * @param position a <code>double</code> representing the position as ratio of the first and last <code>comparables</code>
     * @throws IllegalArgumentException if <code>position</code> is inferior to 0 or superior to 1
     */
    public void setPositionTo(double position);

    /**
     * set the position of the timeline controller. must be a comparable
     *
     * @param position a <code>double</code> representing the position as ratio of the first and last <code>comparables</code>
     * @throws IllegalArgumentException if <code>position</code> is not comprised in the dataset interval
     */
    public void setPositionTo(Comparable position);

    /**
     * TimelineController current position
     *
     * @return a <code>double</code> representing the position as ratio of the first and last <code>comparables</code>
     */
    public double getPosition();

    /**
     * TimelineController status, check is the timeline controller is currently
     * playing
     *
     * @return true if playing, false otherwise
     */
    public boolean isPlaying();

    /**
     * TimelineController status, check is the timeline controller is currently
     * stopped
     *
     * @return true if stopped, false otherwise
     */
    public boolean isStopped();

    /**
     * Set the speed of the timeline
     */
    public void setSpeed(double slicepersecond);

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

    /**
     * Controller name
     *
     * @return a comparable
     */
    public String getName();
}