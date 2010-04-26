/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.api;

import org.gephi.filters.spi.FilterProperty;

/**
 * PropertyExecutor's role is to synchronize property edition with filter execution.
 * When a filter is executed it usually uses properties users can edit. Editing
 * properties values while a filter is executing in another thread could make
 * uncertain behaviour. This executor is responsible to postpone value edition
 * until filter's execution is finished.
 *
 * @author Mathieu Bastian
 * @see FilterProperty
 */
public interface PropertyExecutor {

    /**
     * Set <code>value</code> on <code>property</code> in a safe way by using
     * <code>callback</code>.
     * @param property  the filter property that value is to be set
     * @param value     the value that is to be set
     * @param callback  the callback function to be notified when setting has to
     * be done
     */
    public void setValue(FilterProperty property, Object value, Callback callback);

    /**
     * Callback interface for setting value. When called, setting value is done
     * in a safe window between filter execution.
     */
    public interface Callback {

        public void setValue(Object value);
    }
}
