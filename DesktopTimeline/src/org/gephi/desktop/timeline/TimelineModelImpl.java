/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.timeline;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.timeline.api.TimelineModel;

/**
 *
 * @author jbilcke
 */
public class TimelineModelImpl implements TimelineModel {
    //Model
    private TimelineUI timelineUI;
    private List<Timeline> TimelineList;
    //Listeners
    private List<ChangeListener> listeners;
    
    //
    private Float from;
    private Float to;

    public TimelineModelImpl() {
        listeners = new ArrayList<ChangeListener>();
        TimelineList = new ArrayList<Timeline>();
    }

    public Timeline[] getTimeline() {
        return TimelineList.toArray(new Timeline[0]);
    }

    public void addTimeline(Timeline Timeline) {
        TimelineList.add(Timeline);
        fireChangeEvent();
    }

    public Timeline getTimeline(TimelineUI TimelineUI) {
        Class c = TimelineUI.getTimelineClass();
        for (Timeline b : TimelineList) {
            if (b.getClass().equals(c)) {
                return b;
            }
        }
        return null;
    }



    public void setVisible(TimelineUI TimelineUI, boolean visible) {
        if (visible) {
            if (invisibleList.remove(TimelineUI)) {
                fireChangeEvent();
            }
        } else if (!invisibleList.contains(TimelineUI)) {
            invisibleList.add(TimelineUI);
            fireChangeEvent();
        }
    }

    public void addChangeListener(ChangeListener changeListener) {
        if (!listeners.contains(changeListener)) {
            listeners.add(changeListener);
        }
    }

    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
    }

    public void fireChangeEvent() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(evt);
        }
    } {

}
