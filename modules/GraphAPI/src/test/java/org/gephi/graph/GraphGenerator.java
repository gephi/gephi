package org.gephi.graph;

import java.util.Random;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.WorkspaceImpl;

public class GraphGenerator {

    public static final String INT_COLUMN = "age";
    public static final String DOUBLE_COLUMN = "value";
    public static final String FLOAT_ARRAY_COLUMN = "values";
    public static final String STRING_ARRAY_COLUMN = "array";
    public static final String STRING_COLUMN = "country";
    public static final String TIMESTAMP_SET_COLUMN = "events";
    public static final String INTERVAL_SET_COLUMN = "events";
    public static final String TIMESTAMP_DOUBLE_COLUMN = "price";
    public static final String INTERVAL_DOUBLE_COLUMN = "price";
    public static final String FIRST_NODE = "1";
    public static final String SECOND_NODE = "2";
    public static final String THIRD_NODE = "3";
    public static final String FIRST_EDGE = "1";
    public static final String SECOND_EDGE = "2";
    public static final String[] STRING_COLUMN_VALUES = new String[] {"France", "Germany"};
    public static final float[][] FLOAT_ARRAY_COLUMN_VALUES = new float[][] {{1f, 2f}, {4f, 3f}};
    public static final int INT_COLUMN_MIN_VALUE = 10;
    public static final double[][] TIMESTAMP_DOUBLE_COLUMN_VALUES = new double[][] {{3.0}, {6.0}};
    public static final double[] TIMESTAMP_SET_VALUES = new double[] {3.0, 6.0};
    public static final double[][] INTERVAL_SET_VALUES = new double[][] {{2000.0, 2003.0}, {2002.0, 2005.0}};
    public static final String[][] STRING_ARRAY_COLUMN_VALUES = new String[][] {{"foo", "bar"}, {"foo"}};

    private final GraphModel graphModel;
    private Workspace workspace;

    private GraphGenerator() {
        this(null, new Configuration());
    }

    private GraphGenerator(final Workspace workspace, final Configuration config) {
        GraphModel model = null;
        if (workspace != null) {
            this.workspace = workspace;
            model = workspace.getLookup().lookup(GraphModel.class);
        }
        if (model == null) {
            this.graphModel = GraphModel.Factory.newInstance(config);
        } else {
            this.graphModel = model;
            model.setConfiguration(config);
        }
        if (workspace == null) {
            this.workspace = new WorkspaceImpl(null, 0, "Workspace", graphModel);
        }
    }

    public static GraphGenerator build() {
        return new GraphGenerator();
    }

    public static GraphGenerator build(final Configuration config) {
        return new GraphGenerator(null, config);
    }

    public static GraphGenerator build(final Workspace workspace) {
        return new GraphGenerator(workspace, new Configuration());
    }

    public static GraphGenerator build(final Workspace workspace, final Configuration configuration) {
        return new GraphGenerator(workspace, configuration);
    }

    public GraphGenerator withTimeFormat(TimeFormat timeFormat) {
        graphModel.setTimeFormat(timeFormat);
        return this;
    }

    public GraphGenerator generateTinyGraph() {
        Node n1 = graphModel.factory().newNode(FIRST_NODE);
        Node n2 = graphModel.factory().newNode(SECOND_NODE);
        Edge e = graphModel.factory().newEdge(FIRST_EDGE, n1, n2, 0, 1.0, true);
        graphModel.getDirectedGraph().addNode(n1);
        graphModel.getDirectedGraph().addNode(n2);
        graphModel.getDirectedGraph().addEdge(e);
        return this;
    }

    public GraphGenerator generateTinyUndirectedGraph() {
        Node n1 = graphModel.factory().newNode(FIRST_NODE);
        Node n2 = graphModel.factory().newNode(SECOND_NODE);
        Edge e = graphModel.factory().newEdge(FIRST_EDGE, n1, n2, 0, 1.0, false);
        graphModel.getDirectedGraph().addNode(n1);
        graphModel.getDirectedGraph().addNode(n2);
        graphModel.getDirectedGraph().addEdge(e);
        return this;
    }

    public GraphGenerator generateTinyMixedGraph() {
        Node n1 = graphModel.factory().newNode(FIRST_NODE);
        Node n2 = graphModel.factory().newNode(SECOND_NODE);
        Node n3 = graphModel.factory().newNode(THIRD_NODE);
        Edge e1 = graphModel.factory().newEdge(FIRST_EDGE, n1, n2, 0, 1.0, false);
        Edge e2 = graphModel.factory().newEdge(SECOND_EDGE, n1, n3, 0, 1.0, true);
        graphModel.getGraph().addNode(n1);
        graphModel.getGraph().addNode(n2);
        graphModel.getGraph().addNode(n3);
        graphModel.getGraph().addEdge(e1);
        graphModel.getGraph().addEdge(e2);
        return this;
    }

    public GraphGenerator addNodeLabels() {
        for (Node n : graphModel.getGraph().getNodes()) {
            n.setLabel(n.getId().toString());
        }
        return this;
    }

    public GraphGenerator addEdgeLabels() {
        for (Edge e : graphModel.getGraph().getEdges()) {
            e.setLabel(e.getId().toString());
        }
        return this;
    }

    public GraphGenerator generateTinyMultiGraph() {
        Node n1 = graphModel.factory().newNode(FIRST_NODE);
        Node n2 = graphModel.factory().newNode(SECOND_NODE);
        Edge e1 = graphModel.factory().newEdge(FIRST_EDGE, n1, n2, 0, 1.0, true);
        Edge e2 = graphModel.factory().newEdge(SECOND_EDGE, n1, n2, 1, 1.0, true);
        graphModel.getDirectedGraph().addNode(n1);
        graphModel.getDirectedGraph().addNode(n2);
        graphModel.getDirectedGraph().addEdge(e1);
        graphModel.getDirectedGraph().addEdge(e2);
        return this;
    }

    public GraphGenerator addRandomPositions() {
        Random random = new Random();
        double size = 100.0;
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setX((float) (-size / 2 + size * random.nextDouble()));
            node.setY((float) (-size / 2 + size * random.nextDouble()));
            node.setSize(random.nextFloat() * (float) size / 25f);
        }
        return this;
    }

    public GraphGenerator addIntNodeColumn() {
        graphModel.getNodeTable().addColumn(INT_COLUMN, Integer.class);
        int age = INT_COLUMN_MIN_VALUE;
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(INT_COLUMN, age++);
        }
        return this;
    }

    public GraphGenerator addFloatArrayNodeColumn() {
        graphModel.getNodeTable().addColumn(FLOAT_ARRAY_COLUMN, float[].class);
        Node n1 = graphModel.getGraph().getNode(FIRST_NODE);
        Node n2 = graphModel.getGraph().getNode(SECOND_NODE);
        n1.setAttribute(FLOAT_ARRAY_COLUMN, FLOAT_ARRAY_COLUMN_VALUES[0]);
        n2.setAttribute(FLOAT_ARRAY_COLUMN, FLOAT_ARRAY_COLUMN_VALUES[1]);
        return this;
    }

    public GraphGenerator addStringArrayNodeColumn() {
        graphModel.getNodeTable().addColumn(STRING_ARRAY_COLUMN, String[].class);
        Node n1 = graphModel.getGraph().getNode(FIRST_NODE);
        Node n2 = graphModel.getGraph().getNode(SECOND_NODE);
        n1.setAttribute(STRING_ARRAY_COLUMN, STRING_ARRAY_COLUMN_VALUES[0]);
        n2.setAttribute(STRING_ARRAY_COLUMN, STRING_ARRAY_COLUMN_VALUES[1]);
        return this;
    }

    public GraphGenerator addDoubleNodeColumn() {
        graphModel.getNodeTable().addColumn(DOUBLE_COLUMN, Double.class);
        double val = 10;
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(DOUBLE_COLUMN, val++);
        }
        return this;
    }

    public GraphGenerator addStringNodeColumn() {
        graphModel.getNodeTable().addColumn(STRING_COLUMN, String.class);
        graphModel.getGraph().getNode(FIRST_NODE).setAttribute(STRING_COLUMN, STRING_COLUMN_VALUES[0]);
        graphModel.getGraph().getNode(SECOND_NODE).setAttribute(STRING_COLUMN, STRING_COLUMN_VALUES[1]);
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
        int index = 0;
        double value = 2000;
        if (graphModel.getTimeFormat().equals(TimeFormat.DATE)) {
            value = AttributeUtils.parseDateTime("2022-09-01");
        }
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(TIMESTAMP_DOUBLE_COLUMN, new TimestampDoubleMap(new double[] {value},
                TIMESTAMP_DOUBLE_COLUMN_VALUES[index++]));
        }
        return this;
    }

    public GraphGenerator addTimestampSetColumn() {
        graphModel.getNodeTable().addColumn(TIMESTAMP_SET_COLUMN, TimestampSet.class);
        int index = 0;
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(TIMESTAMP_SET_COLUMN, new TimestampSet(
                new double[] {TIMESTAMP_SET_VALUES[index++]}));
        }
        return this;
    }

    public GraphGenerator setTimestampSet() {
        int index = 0;
        for (Node node : graphModel.getGraph().getNodes()) {
            node.addTimestamp(TIMESTAMP_SET_VALUES[index++]);
        }
        return this;
    }

    public GraphGenerator setIntervalSet() {
        int index = 0;
        for (Node node : graphModel.getGraph().getNodes()) {
            node.addInterval(new Interval(INTERVAL_SET_VALUES[index][0], INTERVAL_SET_VALUES[index][1]));
            index++;
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

    public GraphModel getGraphModel() {
        return graphModel;
    }

    public Workspace getWorkspace() {
        return workspace;
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
            Random random = new Random(42);

            graphModel.getGraph().writeLock();

            Graph graph = graphModel.getGraph();
            for (int i = 0; i < numberOfNodes; i++) {
                Node node = graphModel.factory().newNode(String.valueOf(i));
                graph.addNode(node);
            }

            if (wiringProbability > 0) {
                for (int i = 0; i < numberOfNodes - 1; i++) {
                    Node source = graphModel.getGraph().getNode(String.valueOf(i));
                    for (int j = i + 1; j < numberOfNodes; j++) {
                        Node target = graphModel.getGraph().getNode(String.valueOf(j));

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
}
