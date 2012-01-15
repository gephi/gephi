/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.timeline.api;

import org.gephi.data.attributes.api.AttributeColumn;

/**
 *
 * @author mbastian
 */
public interface TimelineChart {
    
    public AttributeColumn getColumn();
    
    public Number[] getX();
    
    public Number[] getY();
    
    public Number getMinY();
    
    public Number getMaxY();
}
