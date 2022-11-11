package org.gephi.appearance;

import java.awt.Color;
import java.util.Arrays;
import java.util.Optional;
import org.gephi.appearance.api.AttributeFunction;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.netbeans.junit.MockServices;

@RunWith(MockitoJUnitRunner.class)
public class AppearanceControllerTest {

    @Spy
    AppearanceControllerImpl controller = new AppearanceControllerImpl();

    @Test
    public void testTransform() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();

        Assert.assertFalse(controller.getModel().isRankingLocalScale());

        Node node = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);

        Optional<Function> simpleFunction = Arrays.stream(controller.getModel().getNodeFunctions()).filter(
            Function::isSimple).findFirst();
        Assert.assertTrue(simpleFunction.isPresent());

        Optional<Function> rankingFunction =
            Arrays.stream(controller.getModel().getNodeFunctions()).filter(f -> f.isRanking() && f.isAttribute())
                .findFirst();
        Assert.assertTrue(rankingFunction.isPresent());

        Optional<Function> partitionFunction = Arrays.stream(controller.getModel().getNodeFunctions()).filter(
            Function::isPartition).findFirst();
        Assert.assertTrue(partitionFunction.isPresent());

        controller.transform(simpleFunction.get());
        Assert.assertEquals(Color.PINK, node.getColor());

        controller.transform(rankingFunction.get());
        Assert.assertEquals(0, (int) node.size());

        controller.transform(partitionFunction.get());
        Assert.assertEquals(Color.CYAN, node.getColor());
    }

    @Test
    public void testFilteredView() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();

        Node node1 = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        Node node2 = generator.getGraph().getNode(GraphGenerator.SECOND_NODE);

        Function simpleFunction = Arrays.stream(controller.getModel().getNodeFunctions()).filter(
            Function::isSimple).findFirst().get();

        GraphView view = generator.getGraphModel().createView(true, false);
        view.getGraphModel().getGraph(view).addNode(node1);
        generator.getGraphModel().setVisibleView(view);

        controller.transform(simpleFunction);

        Assert.assertEquals(Color.PINK, node1.getColor());
        Assert.assertNotEquals(Color.PINK, node2.getColor());
    }

    @Test
    public void testLocalScale() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();

        Node node1 = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        Node node2 = generator.getGraph().getNode(GraphGenerator.SECOND_NODE);
        int age1 = (Integer) node1.getAttribute(GraphGenerator.INT_COLUMN);
        int age2 = (Integer) node2.getAttribute(GraphGenerator.INT_COLUMN);
        node1.setSize(42f);
        Assert.assertTrue(age2 > age1);

        Function rankingFunction =
            Arrays.stream(controller.getModel().getNodeFunctions()).filter(f -> f.isRanking() && f.isAttribute())
                .findFirst().get();

        GraphView view = generator.getGraphModel().createView(true, false);
        view.getGraphModel().getGraph(view).addNode(node1);
        generator.getGraphModel().setVisibleView(view);

        controller.transform(rankingFunction);
        Assert.assertEquals(0, (int) node1.size());

        controller.setUseRankingLocalScale(true);
        controller.transform(rankingFunction);
        Assert.assertEquals(1, (int) node1.size());
    }

    @Test
    public void testTransformNullValuesRanking() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();
        controller.setTransformNullValues(true);

        Node node = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        node.clearAttributes();
        node.setSize(42f);

        Function rankingFunction =
            Arrays.stream(controller.getModel().getNodeFunctions()).filter(f -> f.isRanking() && f.isAttribute())
                .findFirst().get();
        controller.transform(rankingFunction);
        Assert.assertEquals(0f, node.size(), 0);
    }

    @Test
    public void testNotTransformNullValuesRanking() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();
        controller.setTransformNullValues(false);

        Node node = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        node.clearAttributes();
        node.setSize(42f);

        Function rankingFunction =
            Arrays.stream(controller.getModel().getNodeFunctions()).filter(f -> f.isRanking() && f.isAttribute())
                .findFirst().get();
        controller.transform(rankingFunction);
        Assert.assertEquals(42f, node.size(), 0);
    }

    @Test
    public void testTransformNullValuesPartition() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateSmallRandomGraph().addIntNodeColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();
        controller.setTransformNullValues(true);

        Node node = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        node.clearAttributes();
        node.setColor(Color.GREEN);

        Optional<Function> partitionFunction = Arrays.stream(controller.getModel().getNodeFunctions()).filter(
            Function::isPartition).findFirst();
        controller.transform(partitionFunction.get());
        Assert.assertEquals(Color.CYAN, node.getColor());
    }

    @Test
    public void testNotTransformNullValuesPartition() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateSmallRandomGraph().addIntNodeColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();
        controller.setTransformNullValues(false);

        Node node = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        node.clearAttributes();
        node.setColor(Color.GREEN);

        Optional<Function> partitionFunction = Arrays.stream(controller.getModel().getNodeFunctions()).filter(
            Function::isPartition).findFirst();
        controller.transform(partitionFunction.get());
        Assert.assertEquals(Color.GREEN, node.getColor());
    }

    @Test
    public void testRankingDynamicColumn() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator =
            GraphGenerator.build().withWorkspace().generateTinyGraph().addTimestampDoubleColumn();

        AppearanceModelImpl model = new AppearanceModelImpl(generator.getWorkspace());
        Mockito.doReturn(model).when(controller).getModel();
        Column col = generator.getGraphModel().getNodeTable().getColumn(GraphGenerator.TIMESTAMP_DOUBLE_COLUMN);

        Node node1 = generator.getGraph().getNode(GraphGenerator.FIRST_NODE);
        TimestampDoubleMap ts = new TimestampDoubleMap(new double[] {2000, 2004, 2008}, new double[] {0, 500, 1000});
        node1.setAttribute(col, ts);

        Function rankingFunction =
            Arrays.stream(controller.getModel().getNodeFunctions()).filter(f -> f.isRanking() && f.isAttribute()
                    && ((AttributeFunction) f).getColumn() == col)
                .findFirst().get();

        GraphView view = generator.getGraphModel().createView(true, false);
        view.getGraphModel().getGraph(view).fill();
        generator.getGraphModel().setVisibleView(view);

        controller.transform(rankingFunction);
        Assert.assertEquals(0, (int) node1.size());

        col.setEstimator(Estimator.MAX);
        controller.transform(rankingFunction);
        Assert.assertEquals(1, (int) node1.size());

        view.getGraphModel().setTimeInterval(view, new Interval(2000, 2000));
        controller.transform(rankingFunction);
        Assert.assertEquals(0, (int) node1.size());
    }

    public static class FixedTransformer implements Transformer, RankingTransformer<Node>,
        PartitionTransformer<Node>,
        SimpleTransformer<Node> {

        @Override
        public boolean isNode() {
            return true;
        }

        @Override
        public boolean isEdge() {
            return false;
        }

        @Override
        public void transform(Node node, Partition partition, Object value) {
            node.setColor(Color.CYAN);
        }

        @Override
        public void transform(Node node, Ranking ranking, Number value, float normalisedValue) {
            node.setSize(normalisedValue);
        }

        @Override
        public void transform(Node node) {
            node.setColor(Color.PINK);
        }
    }
}
