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
package org.gephi.layout.api;

import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import java.beans.PropertyChangeListener;
import org.gephi.workspace.api.Workspace;

/**
 * Layout model contains data and flags relative to the layout execution and
 * user interface. There is one model per {@link Workspace}
 * <p>
 * <code>PropertyChangeListener</code> can be used to receive events about
 * a change in the model.
 * @author Mathieu Bastian
 */
public interface LayoutModel {

    public static final String SELECTED_LAYOUT = "selectedLayout";
    public static final String RUNNING = "running";

    /**
     * Returns the currently selected layout or <code>null</code> if no
     * layout is selected.
     */
    public Layout getSelectedLayout();

    /**
     * Return a layout instance for the given <code>layoutBuilder</code>. If
     * saved properties exists, the layout properties values are set. Values
     * are default if it is the first time this layout is built.
     * <p>
     * Use this method instead of <code>LayoutBuilder.buildLayout()</code>
     * directly.
     * @param layoutBuilder the layout builder
     * @return the layout build from <code>layoutBuilder</code> with formely
     * saved properties.
     */
    public Layout getLayout(LayoutBuilder layoutBuilder);

    /**
     * Returns the builder used for building the currently selected layout or
     * <code>null</code> if no layout is selected.
     */
    public LayoutBuilder getSelectedBuilder();

    /**
     * Returns <code>true</code> if a layout is currently running, <code>false</code>
     * otherwise.
     */
    public boolean isRunning();

    /**
     * Add a property change listener for this model. The <code>listener</code>
     * is notified when layout is selected and when running flag change.
     * @param listener a property change listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove listerner.
     * @param listener a property change listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
