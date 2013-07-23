/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.appearance.plugin;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.appearance.spi.Category;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.PartitionTransformerUI;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.ui.appearance.plugin.category.DefaultCategory;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = TransformerUI.class, position = 200)
public class PartitionElementColorTransformerUI implements PartitionTransformerUI {

    private final PartitionColorTransformerPanel panel;

    public PartitionElementColorTransformerUI() {
        panel = new PartitionColorTransformerPanel();
    }

    @Override
    public Category[] getCategories() {
        return new Category[]{DefaultCategory.NODE_COLOR, DefaultCategory.EDGE_COLOR};
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UniqueElementColorTransformerUI.class, "Attribute.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public JPanel getPanel(PartitionTransformer transformer, Partition partition) {
        panel.setup(transformer, partition);
        return panel;
    }

    @Override
    public Class<? extends PartitionTransformer> getTransformerClass() {
        return PartitionElementColorTransformer.class;
    }
}
