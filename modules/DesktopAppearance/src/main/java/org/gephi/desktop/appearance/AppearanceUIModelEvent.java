/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.appearance;

import java.beans.PropertyChangeEvent;

/**
 *
 * @author mbastian
 */
public class AppearanceUIModelEvent extends PropertyChangeEvent {

    public static String MODEL = "model";
    public static String SELECTED_ELEMENT_CLASS = "selectedElementClass";

    public AppearanceUIModelEvent(Object source, String propertyName,
            Object oldValue, Object newValue) {
        super(source, propertyName, newValue, oldValue);
    }
}
