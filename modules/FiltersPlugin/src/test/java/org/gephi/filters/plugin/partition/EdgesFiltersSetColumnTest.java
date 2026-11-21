package org.gephi.filters.plugin.partition;

import org.gephi.appearance.AppearanceModelImpl;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.impl.WorkspaceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.MockServices;

public class EdgesFiltersSetColumnTest {

    private WorkspaceImpl workspace;
    private GraphModel graphModel;

    @Before
    public void setup() {
        MockServices.setServices(DummyTransformer.class);
        GraphGenerator generator =
            GraphGenerator.build().generateTinyGraph().addIntNodeColumn().addStringNodeColumn();
        workspace = (WorkspaceImpl) generator.getWorkspace();
        graphModel = generator.getGraphModel();
        workspace.add(new AppearanceModelImpl(workspace));
    }

    /**
     * Persistence builds the filter from the first builder of the category, then restores the saved column.
     */
    private void assertSetColumnRebindsPartition(FilterBuilder[] builders, Class<?> filterClass) {
        Assert.assertTrue(builders.length >= 2);
        PartitionBuilder.PartitionFilter filter =
            (PartitionBuilder.PartitionFilter) builders[0].getFilter(workspace);
        Assert.assertTrue(filterClass.isInstance(filter));

        Column current = filter.getColumn();
        Column other = graphModel.getNodeTable().getColumn(
            current.getId().equals(GraphGenerator.INT_COLUMN) ? GraphGenerator.STRING_COLUMN :
                GraphGenerator.INT_COLUMN);
        filter.setColumn(other);

        Assert.assertSame(other, filter.getColumn());
    }

    @Test
    public void testIntraEdgesSetColumnRebindsPartition() {
        assertSetColumnRebindsPartition(new IntraEdgesBuilder().getBuilders(workspace),
            IntraEdgesBuilder.IntraEdgesFilter.class);
    }

    @Test
    public void testInterEdgesSetColumnRebindsPartition() {
        assertSetColumnRebindsPartition(new InterEdgesBuilder().getBuilders(workspace),
            InterEdgesBuilder.InterEdgesFilter.class);
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
