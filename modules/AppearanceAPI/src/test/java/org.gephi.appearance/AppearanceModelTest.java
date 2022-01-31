package org.gephi.appearance;

import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
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

        model.getNodeFunctions();

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
