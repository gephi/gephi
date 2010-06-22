/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.event;

import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Bastian
 */
public final class NodeEvent extends AbstractEvent<Node> {

    public NodeEvent(EventType eventType, Node data, GraphView view) {
        super(eventType, view, data);
    }
}
