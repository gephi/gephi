package org.gephi.layout.plugin.tree;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service=LayoutBuilder.class)
public class TreeLayoutBuilder implements LayoutBuilder {

    public String getName() {
        return NbBundle.getMessage(TreeLayoutBuilder.class, "Tree.name");
    }

    public LayoutUI getUI() {
        return new TreeLayoutUI();
    }

    public Layout buildLayout() {
        return new TreeLayout(this);
    }

    private class TreeLayoutUI implements LayoutUI {

        public String getDescription() {
            return NbBundle.getMessage(TreeLayoutBuilder.class, "Tree.description");
        }

        public Icon getIcon() {
            return null;
        }

        public JPanel getSimplePanel(Layout layout) {
            return null;
        }

        public int getQualityRank() {
            return -1;
        }

        public int getSpeedRank() {
            return -1;
        }
        
    }
}
