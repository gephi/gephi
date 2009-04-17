/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.importer.api;

/**
 *
 * @author Mathieu Bastian
 */
public interface ImportContainer {

    public void addNode(NodeDraft nodeDraft);

    public NodeDraft getNode(String id);

    public boolean nodeExists(String id);

    public void addEdge(EdgeDraft edgeDraft);
}
