/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.appearance.plugin;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.plugin.UniqueNodeSizeTransformer;
import org.gephi.appearance.spi.Category;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.SimpleTransformerUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = SimpleTransformerUI.class, position = 200)
public class UniqueNodeSizeTransformerUI implements SimpleTransformerUI {

    @Override
    public Category[] getCategories() {
        return new Category[]{Category.NODE_SIZE};
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UniqueElementColorTransformerUI.class, "Unique.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public JPanel getPanel(SimpleTransformer transformer) {
        return new UniqueSizeTransformerPanel();
    }

    @Override
    public Class<? extends SimpleTransformer> getTransformerClass() {
        return UniqueNodeSizeTransformer.class;
    }
}
