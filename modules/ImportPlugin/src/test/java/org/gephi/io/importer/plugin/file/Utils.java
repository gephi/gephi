package org.gephi.io.importer.plugin.file;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.Importer;
import org.junit.Assert;

public class Utils {

    public static Container importFile(FileImporter importer, String path) {
        importer.setReader(getReader(path));

        Container container = new ImportContainerImpl();
        importer.execute(container.getLoader());

        return container;
    }

    public static NodeDraft[] toNodesArray(Container container) {
        List<NodeDraft> result = new ArrayList<>();
        container.getUnloader().getNodes().iterator().forEachRemaining(result::add);
        return result.toArray(new NodeDraft[0]);
    }

    public static NodeDraft getNode(Container container, String id) {
        NodeDraft[] nodes = toNodesArray(container);
        return Arrays.stream(nodes).filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }

    public static EdgeDraft[] toEdgesArray(Container container) {
        List<EdgeDraft> result = new ArrayList<>();
        container.getUnloader().getEdges().iterator().forEachRemaining(result::add);
        return result.toArray(new EdgeDraft[0]);
    }

    public static void assertSameIds(ElementDraft[] actual, String... ids) {
        Assert.assertEquals(ids.length, actual.length);
        Assert.assertEquals(new HashSet<>(Arrays.asList(ids)),
            Arrays.stream(actual).map(ElementDraft::getId).collect(Collectors.toSet()));
    }

    public static void assertSameEdges(EdgeDraft[] actual, String... edges) {
        Assert.assertEquals(edges.length, actual.length);
        Assert.assertEquals(new HashSet<>(Arrays.asList(edges)),
            Arrays.stream(actual).map(e -> e.getSource().getId()+" -> "+e.getTarget().getId()).collect(Collectors.toSet()));
    }

    public static void assertSameLabels(ElementDraft[] actual, String... labels) {
        Assert.assertEquals(labels.length, actual.length);
        Assert.assertEquals(new HashSet<>(Arrays.asList(labels)),
            Arrays.stream(actual).map(ElementDraft::getLabel).collect(Collectors.toSet()));
    }

    public static Reader getReader(String fileName) {
        try {
            String content = new String(Utils.class.getResourceAsStream(fileName)
                .readAllBytes());
            return new StringReader(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
