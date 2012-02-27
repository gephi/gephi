/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.timeline;

import javax.swing.JComponent;
import org.gephi.desktop.perspective.spi.BottomComponent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service=BottomComponent.class)
public class BottomComponentImpl implements BottomComponent {

    private TimelineTopComponent timelineTopComponent = new TimelineTopComponent();
    
    public JComponent getComponent() {
        return timelineTopComponent;
    }

    public void setVisible(boolean visible) {
        timelineTopComponent.setTimeLineVisible(visible);
    }
    
    public boolean isVisible() {
        return timelineTopComponent.isVisible();
    }
}
