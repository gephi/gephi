/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.spi;

import org.gephi.graph.api.Graph;

/**
 *
 * @author mbastian
 */
public interface RangeFilter extends Filter {
    
    public Number[] getValues(Graph graph);
    
    public FilterProperty getRangeProperty();
    
}
