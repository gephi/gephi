/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
