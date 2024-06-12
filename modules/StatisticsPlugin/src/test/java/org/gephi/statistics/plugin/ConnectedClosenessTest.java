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
    public void testStar() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "Star 30.gexf");
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        ConnectedCloseness cc = new ConnectedCloseness();
        ConnectedCloseness.IndicatorResults indicators = cc.computeConnectedCloseness(graph, false);

        System.out.println("Indicators:");
        System.out.println("Delta: "+indicators.Delta);
        System.out.println("E_percent: "+indicators.E_percent);
        System.out.println("p_percent: "+indicators.p_percent);
        System.out.println("P_edge: "+indicators.P_edge);
        System.out.println("C: "+indicators.C);
    }

    @Test
    public void test3nodes() {
        GraphModel graphModel = GraphImporter.importGraph(DummyTest.class, "3nodes.gexf");
        UndirectedGraph graph = graphModel.getUndirectedGraph();

        ConnectedCloseness cc = new ConnectedCloseness();
        ConnectedCloseness.IndicatorResults indicators = cc.computeConnectedCloseness(graph, false);

        System.out.println("Indicators:");
        System.out.println("Delta: "+indicators.Delta);
        System.out.println("E_percent: "+indicators.E_percent);
        System.out.println("p_percent: "+indicators.p_percent);
        System.out.println("P_edge: "+indicators.P_edge);
        System.out.println("C: "+indicators.C);
    }
}
