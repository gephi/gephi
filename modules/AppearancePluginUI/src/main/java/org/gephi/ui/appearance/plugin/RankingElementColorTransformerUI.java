/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.appearance.plugin;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.spi.Category;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.RankingTransformerUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mbastian
 */
@ServiceProvider(service = RankingTransformerUI.class)
public class RankingElementColorTransformerUI implements RankingTransformerUI {

    private final RankingColorTransformerPanel panel;

    public RankingElementColorTransformerUI() {
        panel = new RankingColorTransformerPanel();
    }

    @Override
    public Category[] getCategories() {
        return new Category[]{Category.NODE_COLOR, Category.EDGE_COLOR};
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UniqueElementColorTransformerUI.class, "Attribute.name");
    }

    @Override
    public JPanel getPanel(RankingTransformer transformer, Number min, Number max) {
        panel.setup(transformer);
        return panel;
    }

    @Override
    public Class<? extends RankingTransformer> getTransformerClass() {
        return RankingElementColorTransformer.class;
    }
}
