/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.event;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphView;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeEvent extends AbstractEvent<Edge> {

    public EdgeEvent(EventType eventType, Edge data, GraphView view) {
        super(eventType, view, data);
    }
}
