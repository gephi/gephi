package org.gephi.visualization;

import java.beans.PropertyChangeEvent;

public interface VizModelPropertyChangeListener extends java.util.EventListener {

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *          and the property that has changed.
     */

    void propertyChange(VizModel model, PropertyChangeEvent evt);
}
