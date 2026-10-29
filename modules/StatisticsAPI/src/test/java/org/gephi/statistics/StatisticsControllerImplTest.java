package org.gephi.statistics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Subgraph;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.api.StatisticsController;
import org.gephi.statistics.spi.DynamicStatistics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

public class StatisticsControllerImplTest {

    private final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);

    @Before
    public void setUp() {
        projectController.newProject();
    }

    @After
    public void tearDown() {
        projectController.closeCurrentProject();
    }

    @Test
    public void testDynamicOnFilteredViewDestroyedDuringRun() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        GraphView filteredView = graphModel.createView();
        Subgraph filteredGraph = graphModel.getGraph(filteredView);
        //Two nodes in each of the windows starting at 0, 2 and 4, one of them filtered out
        for (int i = 0; i < 6; i++) {
            Node node = graphModel.factory().newNode();
            graphModel.getGraph().addNode(node);
            double start = i / 2 * 2;
            node.addInterval(new Interval(start, start + 0.5));
            if (i % 2 == 0) {
                filteredGraph.addNode(node);
            }
        }
        graphModel.setVisibleView(filteredView);

        List<GraphView> windows = new ArrayList<>();
        List<Integer> nodeCounts = new ArrayList<>();
        WindowStatistics statistics = new WindowStatistics() {
            @Override
            public void loop(GraphView window, Interval interval) {
                windows.add(window);
                nodeCounts.add(graphModel.getGraph(window).getNodeCount());
                if (!filteredView.isDestroyed()) {
                    //Simulates a filter pass replacing its result, and destroying the previous one, between two windows
                    graphModel.destroyView(filteredView);
                }
            }
        };
        statistics.setWindow(1);
        statistics.setTick(2);
        statistics.setBounds(new Interval(0, 5));
        Lookup.getDefault().lookup(StatisticsController.class).execute(statistics);

        Assert.assertEquals(Arrays.asList(1, 1, 1), nodeCounts);
        windows.forEach(window -> Assert.assertTrue(window.isDestroyed()));
    }

    private abstract static class WindowStatistics implements DynamicStatistics {

        private double window;
        private double tick;
        private Interval bounds;

        @Override
        public void execute(GraphModel graphModel) {
        }

        @Override
        public void end() {
        }

        @Override
        public String getReport() {
            return "";
        }

        @Override
        public double getWindow() {
            return window;
        }

        @Override
        public void setWindow(double window) {
            this.window = window;
        }

        @Override
        public double getTick() {
            return tick;
        }

        @Override
        public void setTick(double tick) {
            this.tick = tick;
        }

        @Override
        public Interval getBounds() {
            return bounds;
        }

        @Override
        public void setBounds(Interval bounds) {
            this.bounds = bounds;
        }
    }
}
