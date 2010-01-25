/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.filters.plugin.hierarchy;

import javax.swing.JPanel;
import org.gephi.filters.plugin.hierarchy.LevelBuilder.LevelFilter;
import org.gephi.filters.plugin.hierarchy.LevelUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = LevelUI.class)
public class LevelUIImpl implements LevelUI {

    public JPanel getPanel(LevelFilter filter) {
        LevelPanel levelPanel = new LevelPanel();
        levelPanel.setup(filter);
        return levelPanel;
    }
}
