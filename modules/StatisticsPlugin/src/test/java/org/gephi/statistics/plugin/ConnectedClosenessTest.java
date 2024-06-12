package org.gephi.statistics.plugin;

import java.util.HashMap;
import junit.framework.TestCase;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.importer.GraphImporter;
import org.junit.Test;

/**
 * @author Mathieu Jacomy
 */
public class ConnectedClosenessTest extends TestCase {

    @Test
    public void testStable() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "Stable 30.gexf");
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        ConnectedCloseness cc = new ConnectedCloseness();
        ConnectedCloseness.IndicatorResults indicators = cc.computeConnectedCloseness(graph);

        System.out.println(indicators);
        
    }
}
