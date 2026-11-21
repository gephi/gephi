package org.gephi.filters;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.appearance.AppearanceModelImpl;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.PartitionTransformer;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.SimpleTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.filters.plugin.partition.InterEdgesBuilder;
import org.gephi.filters.plugin.partition.IntraEdgesBuilder;
import org.gephi.filters.plugin.partition.PartitionBuilder;
import org.gephi.filters.spi.CategoryBuilder;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.Element;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.WorkspaceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.junit.MockServices;

public class EdgesFiltersPersistenceTest {

    @Before
    public void setup() {
        MockServices.setServices(DummyTransformer.class);
    }

    private static void prepareWorkspace(WorkspaceImpl workspace) {
        GraphGenerator.build(workspace).generateTinyGraph().addIntNodeColumn().addStringNodeColumn();
        workspace.add(new AppearanceModelImpl(workspace));
    }

    private void assertRoundTripKeepsColumn(CategoryBuilder categoryBuilder) throws Exception {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        prepareWorkspace(workspace);
        FilterModelImpl filterModel = workspace.getLookup().lookup(FilterModelImpl.class);

        // Not the first builder, which is the one persistence falls back to
        FilterBuilder builder = categoryBuilder.getBuilders(workspace)[1];
        PartitionBuilder.PartitionFilter filter = (PartitionBuilder.PartitionFilter) builder.getFilter(workspace);
        filterModel.addFirst(new FilterQueryImpl(builder, filter));

        FilterModelPersistenceProvider provider = new FilterModelPersistenceProvider() {
            @Override
            public void readXML(XMLStreamReader reader, Workspace workspace) {
                // A project restores the graph and its columns before the filters
                prepareWorkspace((WorkspaceImpl) workspace);
                super.readXML(reader, workspace);
            }
        };
        Workspace restored = roundTrip(provider, workspace);

        FilterModelImpl restoredModel = restored.getLookup().lookup(FilterModelImpl.class);
        PartitionBuilder.PartitionFilter restoredFilter =
            (PartitionBuilder.PartitionFilter) restoredModel.getQueries()[0].getFilter();
        Assert.assertEquals(filter.getColumn().getId(), restoredFilter.getColumn().getId());
    }

    private static Workspace roundTrip(FilterModelPersistenceProvider provider, Workspace workspace)
        throws Exception {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = XMLOutputFactory.newFactory().createXMLStreamWriter(stringWriter);
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(provider.getIdentifier());
        provider.writeXML(writer, workspace);
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();

        Workspace restored = new WorkspaceImpl(null, 0);
        XMLStreamReader reader = XMLInputFactory.newFactory().createXMLStreamReader(
            new StringReader(stringWriter.toString()));
        provider.readXML(reader, restored);
        reader.close();
        return restored;
    }

    @Test
    public void testIntraEdgesKeepsColumn() throws Exception {
        assertRoundTripKeepsColumn(new IntraEdgesBuilder());
    }

    @Test
    public void testInterEdgesKeepsColumn() throws Exception {
        assertRoundTripKeepsColumn(new InterEdgesBuilder());
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
