/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.gephi.timeline.api;

/**
 *
 * @author jbilcke
 */
public interface TimelineAnimator {

    public void setInterval(double from, double to);

    public void setTo(double to);

    public void setFrom(double from);

    public double getFrom();

    public double getTo();

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
     public void setStepByTick(double s);

    /**
     * get the timeline step by tick
     *
     * @return a float corresponding to the step by tick's value
     */
    public double getStepByTick();


    public void addListener(TimelineAnimatorListener listener);
    public void removeListener(TimelineAnimatorListener listener);
}
