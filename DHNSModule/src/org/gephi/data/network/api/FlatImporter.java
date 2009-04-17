/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.network.api;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public interface FlatImporter {

    public void initImport();

    public void addNode(Node node);

    public void addEdge(Edge edge);

    public void finishImport();
}
