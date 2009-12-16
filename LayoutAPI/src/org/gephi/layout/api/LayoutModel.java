/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.layout.api;

import java.beans.PropertyChangeListener;

/**
 *
 * @author Mathieu Bastian
 */
public interface LayoutModel {

    public static final String SELECTED_LAYOUT = "selectedLayout";
    public static final String RUNNING = "running";

    public Layout getSelectedLayout();

    public Layout getLayout(LayoutBuilder layoutBuilder);

    public LayoutBuilder getSelectedBuilder();

    public boolean isRunning();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
