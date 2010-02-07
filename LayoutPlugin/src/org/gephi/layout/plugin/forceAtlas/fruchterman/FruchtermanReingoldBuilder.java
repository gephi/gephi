/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.layout.plugin.forceAtlas.fruchterman;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.plugin.forceAtlas.ForceAtlas;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = LayoutBuilder.class)
public class FruchtermanReingoldBuilder implements LayoutBuilder {

    private FruchtermanReingoldLayoutUI ui = new FruchtermanReingoldLayoutUI();

    public String getName() {
        return NbBundle.getMessage(FruchtermanReingoldBuilder.class, "name");
    }

    public FruchtermanReingold buildLayout() {
        return new FruchtermanReingold(this);
    }

    public LayoutUI getUI() {
        return ui;
    }

    private static class FruchtermanReingoldLayoutUI implements LayoutUI {

        public String getDescription() {
            return NbBundle.getMessage(FruchtermanReingold.class, "description");
        }

        public Icon getIcon() {
            return null;
        }

        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        public int getQualityRank() {
            return 2;
        }

        public int getSpeedRank() {
            return 3;
        }
    }
}
