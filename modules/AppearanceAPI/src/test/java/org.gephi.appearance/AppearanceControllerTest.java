package org.gephi.appearance;

import java.awt.Color;
import java.util.Arrays;
import java.util.Optional;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.junit.MockServices;

public class AppearanceControllerTest {

    @Test
    public void testTransform() {
        MockServices.setServices(FixedTransformer.class);
        GraphGenerator generator = GraphGenerator.build().withWorkspace().generateTinyGraph().addIntNodeColumn();
        AppearanceControllerImpl controller = new AppearanceControllerImpl();
        Assert.assertFalse(controller.getModel().isLocalScale());

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
        AppearanceControllerImpl controller = new AppearanceControllerImpl();

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
        AppearanceControllerImpl controller = new AppearanceControllerImpl();

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

        controller.setUseLocalScale(true);
        controller.transform(rankingFunction);
        Assert.assertEquals(1, (int) node1.size());
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
