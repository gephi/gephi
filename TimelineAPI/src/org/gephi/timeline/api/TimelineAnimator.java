/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
