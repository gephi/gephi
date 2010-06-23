/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.event;

import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.dhns.core.GraphViewImpl;

/**
 *
 * @author Mathieu Bastian
 */
public final class ViewEvent extends AbstractEvent<GraphView> {

    public ViewEvent(EventType eventType, GraphViewImpl data) {
        super(eventType, data, data);
    }
}
