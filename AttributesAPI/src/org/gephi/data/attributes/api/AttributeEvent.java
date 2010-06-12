/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.api;

/**
 *
 * @author Mathieu Bastian
 */
public interface AttributeEvent {

    /**
     * <ul>
     * <li><b>ADD_COLUMN:</b> A column has been created. Source is the
     * <code>AttributeTable</code> and data is the column.</li>
     * <li><b>REMOVE_COLUMN:</b> A column has been removed. Source is the
     * <code>AttributeTable</code> and data is the column.</li>
     * </ul>
     */
    public enum EventType {

        ADD_COLUMN, REMOVE_COLUMN,
    };

    public EventType getEventType();

    public Object getSource();

    public Object getData();
}
