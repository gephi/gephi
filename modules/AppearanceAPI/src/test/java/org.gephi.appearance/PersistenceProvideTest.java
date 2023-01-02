package org.gephi.appearance;

import java.awt.Color;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.utils.Utils;
import org.gephi.graph.GraphGenerator;
import org.gephi.project.api.Workspace;
import org.gephi.project.io.utils.GephiFormat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PersistenceProvideTest {

    AppearanceModelImpl model;

    @Before
    public void setUp() {
        model = Utils.newAppearanceModel();
    }

    @Test
    public void testEmpty() throws Exception {
        GephiFormat
            .testXMLPersistenceProvider(new AppearanceModelPersistenceProvider(), model.getWorkspace());
    }

    @Test
    public void testInterpolatorLog2() throws Exception {
        model.getDegreeRanking().setInterpolator(Interpolator.LOG2);

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceModelPersistenceProvider(), model.getWorkspace());
        AppearanceModelImpl readModel = workspace.getLookup().lookup(AppearanceModelImpl.class);
        Assert.assertSame(Interpolator.LOG2, readModel.getDegreeRanking().interpolator);
    }

    @Test
    public void testInterpolatorBezier() throws Exception {
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
        model.getEdgeTypePartition().setColor("foo", Color.RED);

        Workspace workspace = GephiFormat
            .testXMLPersistenceProvider(new AppearanceModelPersistenceProvider(), model.getWorkspace());
        AppearanceModelImpl readModel = workspace.getLookup().lookup(AppearanceModelImpl.class);
        Assert.assertEquals(Color.RED, readModel.getEdgeTypePartition().getColor("foo"));
    }
}
