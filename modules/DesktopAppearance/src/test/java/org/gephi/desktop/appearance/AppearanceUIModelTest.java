package org.gephi.desktop.appearance;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.appearance.AppearanceModelImpl;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.plugin.UniqueElementColorTransformer;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerCategory;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Element;
import org.gephi.ui.appearance.plugin.category.DefaultCategory;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.junit.MockServices;
import org.openide.util.NbBundle;

public class AppearanceUIModelTest {

    @Test
    public void testDefault() {
        GraphGenerator generator = GraphGenerator.build().generateTinyGraph();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        AppearanceUIModel uiModel = new AppearanceUIModel(model);

        Assert.assertSame(DefaultCategory.COLOR, uiModel.getSelectedCategory());
        Assert.assertSame(AppearanceUIController.NODE_ELEMENT, uiModel.getSelectedElementClass());
        Function selectedFunction = uiModel.getSelectedFunction();
        Assert.assertNotNull(selectedFunction);

        Assert.assertEquals(UniqueElementColorTransformer.class, selectedFunction.getTransformer().getClass());
    }
}
