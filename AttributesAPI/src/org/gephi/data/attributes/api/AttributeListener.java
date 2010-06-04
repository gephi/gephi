/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes.api;

import java.util.EventListener;

/**
 *
 * @author Mathieu Bastian
 */
public interface AttributeListener extends EventListener {

    public void attributesChanged(AttributeEvent event);
}
