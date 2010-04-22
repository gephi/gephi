/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.filters.plugin.graph;

import javax.swing.JPanel;
import org.gephi.filters.plugin.graph.EgoBuilder.EgoFilter;
import org.gephi.filters.plugin.graph.EgoUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = EgoUI.class)
public class EgoUIImpl implements EgoUI {

    public JPanel getPanel(EgoFilter egoFilter) {
        EgoPanel egoPanel = new EgoPanel();
        egoPanel.setup(egoFilter);
        return egoPanel;
    }
}
