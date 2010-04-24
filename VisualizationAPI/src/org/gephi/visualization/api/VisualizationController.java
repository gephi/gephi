/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.visualization.api;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public interface VisualizationController {

    public void selectNodes(Node[] nodes);

    public void selectEdges(Edge[] edges);
}
