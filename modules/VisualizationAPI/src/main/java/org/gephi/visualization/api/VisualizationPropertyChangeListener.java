package org.gephi.visualization.api;

import java.beans.PropertyChangeEvent;

public interface VisualizationPropertyChangeListener extends java.util.EventListener {

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *          and the property that has changed.
     */

    void propertyChange(VisualisationModel model, PropertyChangeEvent evt);
}
