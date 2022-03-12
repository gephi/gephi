package org.gephi.appearance;

import java.awt.Color;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.utils.Utils;
import org.gephi.graph.GraphGenerator;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.Assert;
import org.junit.Test;

public class PersistenceProvideTest {

    @Test
    public void testEmpty() throws Exception {
        AppearanceModelImpl model = Utils.newAppearanceModel();

        GephiFormat
            .testXMLPersistenceProvider(new AppearanceModelPersistenceProvider(), model.getWorkspace());
    }

    @Test
    public void testInterpolatorLog2() throws Exception {
        AppearanceModelImpl model = Utils.newAppearanceModel();
        model.getDegreeRanking().setInterpolator(Interpolator.LOG2);

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceModelPersistenceProvider(), model.getWorkspace());
        AppearanceModelImpl readModel = workspace.getLookup().lookup(AppearanceModelImpl.class);
        Assert.assertSame(Interpolator.LOG2, readModel.getDegreeRanking().interpolator);
    }

    @Test
    public void testInterpolatorBezier() throws Exception {
        AppearanceModelImpl model = Utils.newAppearanceModel();
        model.getDegreeRanking()
            .setInterpolator(Interpolator.newBezierInterpolator(0.23f, 0.45f, 0.67f, 0.12f));

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceModelPersistenceProvider(), model.getWorkspace());
        AppearanceModelImpl readModel = workspace.getLookup().lookup(AppearanceModelImpl.class);
        Assert.assertEquals(model.getDegreeRanking().interpolator,
            readModel.getDegreeRanking().interpolator);
    }

    @Test
    public void testPartitionColor() throws Exception {
        AppearanceModelImpl model = Utils.newAppearanceModel();
        model.getEdgeTypePartition().setColor("foo", Color.RED);

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceModelPersistenceProvider(), model.getWorkspace());
        AppearanceModelImpl readModel = workspace.getLookup().lookup(AppearanceModelImpl.class);
        Assert.assertEquals(Color.RED, readModel.getEdgeTypePartition().getColor("foo"));
    }
}
