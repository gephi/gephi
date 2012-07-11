/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.perspective.spi;

import javax.swing.JComponent;

/**
 *
 * @author mbastian
 */
public interface BottomComponent {
    
    public JComponent getComponent();
    
    public void setVisible(boolean visible);
    
    public boolean isVisible();
}
