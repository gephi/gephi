/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.appearance.plugin;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.plugin.UniqueElementColorTransformer;
import org.gephi.appearance.spi.Category;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.SimpleTransformerUI;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.ui.appearance.plugin.category.DefaultCategory;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = TransformerUI.class, position = 100)
public class UniqueElementColorTransformerUI implements SimpleTransformerUI {

    @Override
    public Category[] getCategories() {
        return new Category[]{DefaultCategory.NODE_COLOR, DefaultCategory.EDGE_COLOR};
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UniqueElementColorTransformerUI.class, "Unique.name");
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public JPanel getPanel(SimpleTransformer transformer) {
        return new UniqueColorTransformerPanel();
    }

    @Override
    public Class<? extends SimpleTransformer> getTransformerClass() {
        return UniqueElementColorTransformer.class;
    }
}
