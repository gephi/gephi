/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.filters.plugin.graph;

import javax.swing.JPanel;

/**
 *
 * @author Mathieu Bastian
 */
public interface EgoUI {

    public JPanel getPanel(EgoBuilder.EgoFilter egoFilter);
}
