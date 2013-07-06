/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.statistics.plugin;

import org.gephi.graph.api.GraphModel;

/**
 *
 * @author Anna
 */
public interface GraphGenerator {
    
    GraphModel generateNullUndirectedGraph(int n);
    GraphModel generateCompleteUndirectedGraph(int n);
    GraphModel generatePathUndirectedGraph(int n);
    GraphModel generateCyclicUndirectedGraph(int n);
    GraphModel generateStarUndirectedGraph(int n);
    
    GraphModel generateNullDirectedGraph(int n);
    GraphModel generateCompleteDirectedGraph(int n);
    GraphModel generatePathDirectedGraph(int n);
    GraphModel generateCyclicDirectedGraph(int n);
}
