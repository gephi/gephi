/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.dhns.event;

import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.api.GraphView;

/**
 *
 * @author Mathieu Bastian
 */
public class GeneralEvent extends AbstractEvent {

    public GeneralEvent(EventType eventType, GraphView view) {
        super(eventType, view, null);
    }
}

