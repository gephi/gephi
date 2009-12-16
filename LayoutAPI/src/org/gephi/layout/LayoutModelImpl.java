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
package org.gephi.layout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.layout.api.Layout;
import org.gephi.layout.api.LayoutBuilder;
import org.gephi.layout.api.LayoutModel;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.longtask.LongTaskExecutor;
import org.gephi.utils.longtask.LongTaskListener;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class LayoutModelImpl implements LayoutModel {

    //Listeners
    private List<PropertyChangeListener> listeners;
    //Data
    private Layout selectedLayout;
    private LayoutBuilder selectedBuilder;
    //Util
    private LongTaskExecutor executor;

    public LayoutModelImpl() {
        listeners = new ArrayList<PropertyChangeListener>();

        executor = new LongTaskExecutor(true, "layout", 5);
        executor.setLongTaskListener(new LongTaskListener() {

            public void taskFinished(LongTask task) {
                setRunning(false);
            }
        });
    }

    public Layout getSelectedLayout() {
        return selectedLayout;
    }

    public LayoutBuilder getSelectedBuilder() {
        return selectedBuilder;
    }

    public Layout getLayout(LayoutBuilder layoutBuilder) {
        Layout layout = layoutBuilder.buildLayout();
        selectedBuilder = layoutBuilder;
        layout.resetPropertiesValues();
        //Push saved properties
        return layout;
    }

    protected void setSelectedLayout(Layout selectedLayout) {
        Layout oldValue = this.selectedLayout;
        this.selectedLayout = selectedLayout;
        firePropertyChangeEvent(SELECTED_LAYOUT, oldValue, selectedLayout);
    }

    public boolean isRunning() {
        return executor.isRunning();
    }

    protected void setRunning(boolean running) {
        firePropertyChangeEvent(RUNNING, !running, running);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
        PropertyChangeEvent evt = null;
        if (propertyName.equals(SELECTED_LAYOUT)) {
            evt = new PropertyChangeEvent(this, SELECTED_LAYOUT, oldValue, newValue);
        } else if (propertyName.equals(RUNNING)) {
            evt = new PropertyChangeEvent(this, RUNNING, oldValue, newValue);
        } else {
            return;
        }
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(evt);
        }
    }

    public LongTaskExecutor getExecutor() {
        return executor;
    }
}
