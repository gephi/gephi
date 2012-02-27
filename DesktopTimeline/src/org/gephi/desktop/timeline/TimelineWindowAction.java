/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.timeline;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.util.Lookup;

public final class TimelineWindowAction implements ActionListener {
    
    public void actionPerformed(ActionEvent e) {
        BottomComponentImpl bottomComponent = Lookup.getDefault().lookup(BottomComponentImpl.class);
        if (bottomComponent != null) {
            bottomComponent.setVisible(!bottomComponent.isVisible());
        }
    }
}
