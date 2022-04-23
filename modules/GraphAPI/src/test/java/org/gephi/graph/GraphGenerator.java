package org.gephi.graph;

import java.util.Random;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.openide.util.Lookup;

public class GraphGenerator {

    public static final String INT_COLUMN = "age";
    public static final String DOUBLE_COLUMN = "value";
    public static final String FLOAT_ARRAY_COLUMN = "values";
    public static final String STRING_COLUMN = "country";
    public static final String TIMESTAMP_SET_COLUMN = "events";
    public static final String INTERVAL_SET_COLUMN = "events";
    public static final String TIMESTAMP_DOUBLE_COLUMN = "price";
    public static final String INTERVAL_DOUBLE_COLUMN = "price";
    public static final String FIRST_NODE = "1";
    public static final String SECOND_NODE = "2";
    public static final String FIRST_EDGE = "1";
    public static final String SECOND_EDGE = "2";
    public static final String[] STRING_COLUMN_VALUES = new String[] {"France", "Germany"};
    public static final float[][] FLOAT_ARRAY_COLUMN_VALUES = new float[][] {{1f, 2f}, {4f, 3f}};
    public static final int INT_COLUMN_MIN_VALUE = 10;
    public static final double[][] TIMESTAMP_DOUBLE_COLUMN_VALUES = new double[][] {{3.0}, {6.0}};

    private final GraphModel graphModel;
    private Workspace workspace;

    private GraphGenerator() {
        this(new Configuration());
    }

    private GraphGenerator(final Configuration config) {
        this.graphModel = GraphModel.Factory.newInstance(config);
    }

    public static GraphGenerator build() {
        return new GraphGenerator();
    }

    public static GraphGenerator build(Configuration configuration) {
        return new GraphGenerator(configuration);
    }

    public GraphGenerator withProject() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        workspace = projectController.getCurrentWorkspace();
        workspace.add(graphModel);
        return this;
    }

    public GraphGenerator withWorkspace() {
        workspace = new WorkspaceImpl(null, 0);
        workspace.add(graphModel);
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
        for (Node node : graphModel.getGraph().getNodes()) {
            node.setAttribute(TIMESTAMP_DOUBLE_COLUMN, new TimestampDoubleMap(new double[] {2000},
                TIMESTAMP_DOUBLE_COLUMN_VALUES[index++]));
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
