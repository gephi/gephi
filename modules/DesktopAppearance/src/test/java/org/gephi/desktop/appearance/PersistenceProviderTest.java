package org.gephi.desktop.appearance;

import org.gephi.appearance.api.Function;
import org.gephi.appearance.plugin.RankingElementColorTransformer;
import org.gephi.appearance.plugin.RankingNodeSizeTransformer;
import org.gephi.appearance.plugin.UniqueElementColorTransformer;
import org.gephi.appearance.plugin.UniqueNodeSizeTransformer;
import org.gephi.desktop.appearance.utils.Utils;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.gephi.ui.appearance.plugin.RankingElementColorTransformerUI;
import org.gephi.ui.appearance.plugin.RankingElementSizeTransformerUI;
import org.gephi.ui.appearance.plugin.category.DefaultCategory;
import org.junit.Assert;
import org.junit.Test;
import org.openide.util.Lookup;

public class PersistenceProviderTest {

    @Test
    public void testEmpty() throws Exception {
        AppearanceUIModel model = Utils.newAppearanceUIModel();

        GephiFormat
            .testXMLPersistenceProvider(new AppearanceUIModelPersistenceProvider(), model.getWorkspace());
    }

    @Test
    public void testUniqueColor() throws Exception {
        AppearanceUIModel model = Utils.newAppearanceUIModel();
        Function function = Utils.findNodeFunction(model, UniqueElementColorTransformer.class);
        model.setSelectedFunction(function);
        Assert.assertNotNull(function);
        Assert.assertFalse(model.savedProperties.isEmpty());

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceUIModelPersistenceProvider(), model.getWorkspace());
        AppearanceUIModel readModel = workspace.getLookup().lookup(AppearanceUIModel.class);
        Assert.assertEquals(model.savedProperties, readModel.savedProperties);
    }

    @Test
    public void testUniqueSize() throws Exception {
        AppearanceUIModel model = Utils.newAppearanceUIModel();
        Function function = Utils.findNodeFunction(model, UniqueNodeSizeTransformer.class);
        Assert.assertNotNull(function);
        model.setSelectedCategory(DefaultCategory.SIZE);
        model.setSelectedFunction(function);
        model.saveTransformerProperties();
        Assert.assertTrue(model.savedProperties.containsKey(function));

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceUIModelPersistenceProvider(), model.getWorkspace());
        AppearanceUIModel readModel = workspace.getLookup().lookup(AppearanceUIModel.class);
        Assert.assertEquals(model.savedProperties, readModel.savedProperties);
    }

    @Test
    public void testRankingColor() throws Exception {
        AppearanceUIModel model = Utils.newAppearanceUIModel();
        Function function = Utils.findNodeFunction(model, RankingElementColorTransformer.class);
        Assert.assertNotNull(function);
        model.setSelectedTransformerUI(Lookup.getDefault().lookup(RankingElementColorTransformerUI.class));
        model.setSelectedFunction(function);
        model.saveTransformerProperties();
        Assert.assertTrue(model.savedProperties.containsKey(function));

        GephiFormat
            .testXMLPersistenceProvider(new AppearanceUIModelPersistenceProvider(), model.getWorkspace());
    }

    @Test
    public void testRankingSize() throws Exception {
        AppearanceUIModel model = Utils.newAppearanceUIModel();
        Function function = Utils.findNodeFunction(model, RankingNodeSizeTransformer.class);
        Assert.assertNotNull(function);
        model.setSelectedCategory(DefaultCategory.SIZE);
        model.setSelectedTransformerUI(Lookup.getDefault().lookup(RankingElementSizeTransformerUI.class));
        model.setSelectedFunction(function);
        Assert.assertFalse(model.savedProperties.isEmpty());

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceUIModelPersistenceProvider(), model.getWorkspace());
        AppearanceUIModel readModel = workspace.getLookup().lookup(AppearanceUIModel.class);
        Assert.assertEquals(model.savedProperties, readModel.savedProperties);
    }
}
