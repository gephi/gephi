package org.gephi.io.importer.plugin.file;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.ElementDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.junit.Assert;

public class Utils {

    public static NodeDraft[] toNodesArray(ContainerUnloader containerUnloader) {
        List<NodeDraft> result = new ArrayList<>();
        containerUnloader.getNodes().iterator().forEachRemaining(result::add);
        return result.toArray(new NodeDraft[0]);
    }

    public static EdgeDraft[] toEdgesArray(ContainerUnloader containerUnloader) {
        List<EdgeDraft> result = new ArrayList<>();
        containerUnloader.getEdges().iterator().forEachRemaining(result::add);
        return result.toArray(new EdgeDraft[0]);
    }

    public static void assertSameIds(ElementDraft[] actual, String... ids) {
        Assert.assertEquals(ids.length, actual.length);
        Assert.assertEquals(Arrays.stream(actual).map(ElementDraft::getId).collect(Collectors.toSet()),
            new HashSet<>(Arrays.asList(ids)));
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
