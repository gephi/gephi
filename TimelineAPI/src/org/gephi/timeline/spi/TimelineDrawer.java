/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.timeline.spi;

import java.awt.MenuContainer;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import javax.accessibility.Accessible;
import org.gephi.timeline.api.TimelineModel;

/**
 *
 * @author jbilcke
 */
public interface TimelineDrawer
        extends Accessible,
        ImageObserver,
        MenuContainer,
        Serializable {

    public void setModel(TimelineModel model);
    public TimelineModel getModel();
    
}
