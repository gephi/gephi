/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.network.api;

import org.gephi.graph.api.Sight;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public interface FreeModifier {

    public void expand(Node node, Sight sight);

    public void retract(Node node, Sight sight);

    public void addNode(Node node, Node parent);

    public void deleteNode(Node node);

    public void addEdge(Edge edge);

    public void deleteEdge(Edge edge);
}
