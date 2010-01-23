/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.api;

import org.gephi.filters.spi.FilterProperty;

/**
 *
 * @author Mathieu Bastian
 */
public interface PropertyExecutor {

    public void setValue(FilterProperty property, Object value, Callback callback);

    public interface Callback {

        public void setValue(Object value);
    }
}
