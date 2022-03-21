package org.gephi.appearance;

import java.util.Arrays;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.AttributeFunction;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.GraphFunction;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphView;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.junit.MockServices;

public class AppearanceModelTest {

    @Test
    public void testDefaultPartition() {
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());

        Column idCol = generator.getGraphModel().getNodeTable().getColumn("id");
        Assert.assertNull(model.getNodePartition(idCol));

        Column ageCol = generator.getGraphModel().getNodeTable().getColumn("id");
        Assert.assertNull(model.getNodePartition(ageCol));

        Assert.assertNotNull(model.getDegreeRanking());
    }

    @Test
    public void testPartitionColumnCreation() {
        MockServices.setServices(DummyTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());

        Column ageCol = generator.getGraphModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);
        Assert.assertNotNull(model.getNodePartition(ageCol));

        Partition partition = model.getNodePartition(ageCol);
        Assert.assertEquals(generator.getGraph().getNodeCount(), partition.getElementCount(generator.getGraph()));
    }

    @Test
    public void testPartitionCleanup() {
        MockServices.setServices(DummyTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        model.getNodeFunctions();

        generator.getGraphModel().getNodeTable().removeColumn(GraphGenerator.INT_COLUMN);
        System.gc();
        System.runFinalization();

        Assert.assertEquals(0, model.countNodeAttributeRanking());
    }

    @Test
    public void testHasChanged() {
        MockServices.setServices(DummyTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Function function =
            Arrays.stream(model.getNodeFunctions()).filter(f -> f.isPartition() && f.isAttribute()).findFirst().get();

        Assert.assertFalse(function.hasChanged());
        GraphView view = generator.getGraphModel().createView();
        generator.getGraphModel().setVisibleView(view);
        Assert.assertFalse(function.hasChanged());

        model.setPartitionLocalScale(true);
        Assert.assertTrue(function.hasChanged());
        Assert.assertFalse(function.hasChanged());
    }

    @Test
    public void testNodeFunctionsDegree() {
        MockServices.setServices(DummyTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());

        Column col = generator.getGraphModel().defaultColumns().degree();
        Function function = model.getNodeFunction(col, DummyTransformer.class);
        Assert.assertNotNull(function);
        Assert.assertTrue(function instanceof GraphFunction);
        Assert
            .assertEquals(AppearanceModel.GraphFunction.NODE_DEGREE, ((GraphFunctionImpl) function).getGraphFunction());
    }

    @Test
    public void testNodeFunctionsAttribute() {
        MockServices.setServices(DummyTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());

        Column col = generator.getGraphModel().getNodeTable().getColumn(GraphGenerator.INT_COLUMN);
        Function function = model.getNodeFunction(col, DummyTransformer.class);
        Assert.assertNotNull(function);
        Assert.assertTrue(function.isAttribute());
        Assert.assertEquals(col, ((AttributeFunction) function).getColumn());
    }

    @Test
    public void testEdgeFunctionsWeight() {
        MockServices.setServices(DummyTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());

        Column col = generator.getGraphModel().getEdgeTable().getColumn("weight");
        Function function = model.getEdgeFunction(col, DummyTransformer.class);
        Assert
            .assertEquals(AppearanceModel.GraphFunction.EDGE_WEIGHT, ((GraphFunctionImpl) function).getGraphFunction());
    }

    public static class DummyTransformer implements Transformer, RankingTransformer, PartitionTransformer,
        SimpleTransformer {

        @Override
        public boolean isNode() {
            return true;
        }

        @Override
        public boolean isEdge() {
            return true;
        }

        @Override
        public void transform(Element element, Partition partition, Object value) {

        }

        @Override
        public void transform(Element element, Ranking ranking, Number value, float normalisedValue) {

        }

        @Override
        public void transform(Element element) {

        }
    }
}
