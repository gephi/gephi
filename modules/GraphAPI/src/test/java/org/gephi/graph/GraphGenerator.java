package org.gephi.graph;

import java.util.Random;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.project.api.Workspace;
import org.netbeans.junit.MockServices;

public class GraphGenerator {

    public static final String INT_COLUMN = "age";
    public static final String TIMESTAMP_SET_COLUMN = "events";
    public static final String INTERVAL_SET_COLUMN = "events";
    public static final String TIMESTAMP_DOUBLE_COLUMN = "price";
    public static final String INTERVAL_DOUBLE_COLUMN = "price";

    private final GraphModel graphModel;

    private GraphGenerator() {
        this(new Configuration());
    }

    private GraphGenerator(final Configuration config) {
        this.graphModel = GraphModel.Factory.newInstance(config);
    }

    public static GraphGenerator build() {
        return new GraphGenerator();
    }

    public static GraphGenerator buildAndMock() {
        return build().mockController();
    }

    public static GraphGenerator build(Configuration configuration) {
        return new GraphGenerator(configuration);
    }

    public static GraphGenerator buildAndMock(Configuration configuration) {
        return build(configuration).mockController();
    }

    public GraphGenerator mockController() {
        MockServices.setServices(MockGraphController.class);
        return this;
    }

    public GraphGenerator generateTinyGraph() {
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Edge e = graphModel.factory().newEdge(n1, n2);
        graphModel.getDirectedGraph().addNode(n1);
        graphModel.getDirectedGraph().addNode(n2);
        graphModel.getDirectedGraph().addEdge(e);
        return this;
    }

    public GraphGenerator addIntNodeColumn() {
        graphModel.getNodeTable().addColumn(INT_COLUMN, Integer.class);
        int age = 10;
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(INT_COLUMN, age++);
        }
        return this;
    }

    public GraphGenerator addIntervalDoubleColumn() {
        graphModel.getNodeTable().addColumn(INTERVAL_DOUBLE_COLUMN, IntervalDoubleMap.class);
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(INTERVAL_DOUBLE_COLUMN, new IntervalDoubleMap(new double[] {2000, 2001},
                new double[] {Math.random() * 100.0}));
        }
        return this;
    }

    public GraphGenerator addTimestampDoubleColumn() {
        graphModel.getNodeTable().addColumn(TIMESTAMP_DOUBLE_COLUMN, TimestampDoubleMap.class);
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(TIMESTAMP_DOUBLE_COLUMN, new TimestampDoubleMap(new double[] {2000},
                new double[] {Math.random() * 100.0}));
        }
        return this;
    }

    public GraphGenerator addTimestampSetColumn() {
        graphModel.getNodeTable().addColumn(TIMESTAMP_SET_COLUMN, TimestampSet.class);
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(TIMESTAMP_SET_COLUMN, new TimestampSet(
                new double[] {Math.random() * 100.0}));
        }
        return this;
    }

    public GraphGenerator addIntervalSetColumn() {
        graphModel.getNodeTable().addColumn(INTERVAL_SET_COLUMN, TimestampSet.class);
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(INTERVAL_SET_COLUMN, new IntervalSet(
                new double[] {2000, 2001}));
        }
        return this;
    }

    public GraphGenerator generateSmallRandomGraph() {
        new RandomGraph(100, 0.01).generate();
        return this;
    }

    public Graph getGraph() {
        return graphModel.getGraph();
    }

    private class RandomGraph {

        protected final int numberOfNodes;
        protected final int numberOfEdges;
        protected final double wiringProbability;

        public RandomGraph(int n, double p) {
            numberOfNodes = n;
            numberOfEdges = (int) (n * (n - 1) * p);
            wiringProbability = p;
        }

        public RandomGraph(int nodes, int edges) {
            this(nodes, ((double) edges) / (nodes * (nodes - 1)));
        }

        public Graph generate() {
            Random random = new Random();

            graphModel.getGraph().writeLock();

            Graph graph = graphModel.getGraph();
            for (int i = 0; i < numberOfNodes; i++) {
                Node node = graphModel.factory().newNode(i);
                graph.addNode(node);
            }

            if (wiringProbability > 0) {
                for (int i = 0; i < numberOfNodes - 1; i++) {
                    Node source = graphModel.getGraph().getNode(i);
                    for (int j = i + 1; j < numberOfNodes; j++) {
                        Node target = graphModel.getGraph().getNode(j);

                        if (random.nextDouble() < wiringProbability && source != target) {
                            Edge edge = graphModel.factory().newEdge(source, target, 0, true);
                            graph.addEdge(edge);
                        }
                    }
                }
            }

            graphModel.getGraph().writeUnlock();
            return graph;
        }
    }

    private abstract class MockGraphController implements GraphController {

        @Override
        public GraphModel getGraphModel() {
            return graphModel;
        }

        @Override
        public GraphModel getGraphModel(Workspace workspace) {
            return graphModel;
        }
    }
}
