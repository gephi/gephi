package org.gephi.visualization.api;

import java.util.EventListener;

public interface VisualizationEventListener extends EventListener {

    boolean handleEvent(VisualizationEvent event);

    VisualizationEvent.Type getType();
}
