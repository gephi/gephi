/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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
package org.gephi.desktop.timeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import org.gephi.timeline.api.TimelineAnimator;
import org.gephi.timeline.api.TimelineAnimatorListener;
import org.gephi.timeline.api.TimelinePlayMode;

/**
 *
 * @author Julian Bilcke
 */
public class TimelineAnimatorImpl 
        implements TimelineAnimator, ActionListener {

    //Listeners
    private List<TimelineAnimatorListener> listeners;

    //
    private Timer timer;

    private double relativeSelectionStart;
    private double relativeSelectionEnd;


    private TimelinePlayMode playMode;
    private double stepByTick;
    private AtomicBoolean paused;

    public TimelineAnimatorImpl() {
        listeners = new ArrayList<TimelineAnimatorListener>();
        playMode = playMode.OLDEST;
        stepByTick = 0.01;
        paused = new AtomicBoolean(true);

        relativeSelectionStart = 0.0;
        relativeSelectionEnd = 1.0;
    }

    public synchronized void setInterval(double from, double to) {
        setFrom(from);
        setTo(to);
    }

    public synchronized void setTo(double to) {

        if (to > 1.0) {
            relativeSelectionStart = 1.0;
        }
        else if (to <= 0.0) {
            relativeSelectionStart = 0.0;
        } else {
        relativeSelectionStart = to;
        }
    }

    public synchronized void setFrom(double from) {

        if (from > 1.0) {
            relativeSelectionEnd = 1.0;
        }
        else if (from <= 0.0) {
            relativeSelectionEnd = 0.0;
        } else {
        relativeSelectionEnd = from;
        }
    }
    public synchronized double getFrom() {
        return relativeSelectionStart;
    }

    public synchronized double getTo() {
        return relativeSelectionEnd;
    }

    public synchronized void play() {
        timer = new Timer(100, this); // 0.1
        timer.setInitialDelay(1900); // 1.9 sec
        paused.set(false);
        timer.start();
    }

    public synchronized void play(TimelinePlayMode playMode) {
        setTimelinePlayMode(playMode);
        play();
    }

    public synchronized boolean togglePause() {
        boolean p = !paused.get();
        paused.set(p);
        return p;
    }

    public synchronized void setPause(boolean p) {
        paused.set(p);
    }

    public synchronized void stop() {
         timer.stop();
    }

    public synchronized boolean isPaused() {
        return paused.get();
    }
     public synchronized boolean isStopped() {
        return !timer.isRunning();
    }

   

    public synchronized void setTimelinePlayMode(TimelinePlayMode playMode) {
        this.playMode = playMode;
    }
    public synchronized TimelinePlayMode getTimelinePlayMode() {
      return playMode;
    }


    
   public synchronized void setStepByTick(double s) {
        stepByTick = s;
    }
    public synchronized double getStepByTick() {
      return stepByTick;
    }

    public void addListener(TimelineAnimatorListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TimelineAnimatorListener listener) {
        listeners.remove(listener);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (TimelineAnimatorListener listener : listeners) {
            listener.timelineAnimatorChanged(evt);
        }
    }


    public synchronized void actionPerformed(ActionEvent ae) {
        if (paused.get()) return;

        double s = getStepByTick();

        double f = getFrom();
        double t = getTo();

        switch(getTimelinePlayMode()) {
            case YOUNGEST:
                f += s;
                if (f >= 0.95) {
                    stop();
                    f = 0.95f;
                }
                break;
            case BOTH:
                f += s;
                t += s;
                if (t >= 1.0) {
                    stop();
                    t = 1.0f;
                    f -= s;
                }
                break;
            case OLDEST:
                t += s;
                if (t >= 1.0) {
                    stop();
                    t = 1.0f;
                 }
                break;
        }
        setInterval(f, t);
        fireChangeEvent();
    }

}
