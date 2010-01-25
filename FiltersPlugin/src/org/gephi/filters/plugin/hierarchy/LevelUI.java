/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin.hierarchy;

import javax.swing.JPanel;
import org.gephi.filters.plugin.hierarchy.LevelBuilder.LevelFilter;

/**
 *
 * @author Mathieu Bastian
 */
public interface LevelUI {

    public JPanel getPanel(LevelFilter filter);
}
