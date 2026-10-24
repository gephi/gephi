package org.gephi.releasetests;

import java.io.File;
import java.time.Duration;
import javax.imageio.ImageIO;
import javax.swing.UIManager;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.graph.DegreeRangeBuilder;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.Modularity;

/**
 * A short pass over the main features with the Overview showing: the look and feel, then import, layout, statistics,
 * filter, screenshot and export, and the project saved and reopened. It checks the release tests themselves work,
 * and that none of these fails, logs an error or blocks Gephi.
 */
final class SmokeSuite implements Suite {

    @Override
    public void run(SuiteContext c) throws Exception {
        c.step("look and feel", () -> {
            String lookAndFeel = UIManager.getLookAndFeel().getName();
            SuiteContext.check(lookAndFeel.startsWith("FlatLaf"), "Gephi runs with " + lookAndFeel + ", not FlatLaf");
        });

        Workspace workspace = c.step("import", () -> c.importResource("LesMiserables.gexf"));
        GraphModel graphModel = c.graphController.getGraphModel(workspace);
        int nodeCount = graphModel.getGraph().getNodeCount();
        int edgeCount = graphModel.getGraph().getEdgeCount();
        c.expectRendering(true);

        c.step("layout", () -> c.runLayout(ForceAtlas2Builder.class, Duration.ofSeconds(5)));

        c.step("statistics", () -> {
            c.runStatistics(new Degree());
            c.runStatistics(new Modularity());
        });

        c.step("filter", () -> {
            DegreeRangeBuilder.DegreeRangeFilter filter = new DegreeRangeBuilder.DegreeRangeFilter();
            filter.init(graphModel.getGraph());
            filter.setRange(new Range(5, Integer.MAX_VALUE));
            Query query = c.filterController.createQuery(filter);
            SuiteContext.onEdt(() -> c.filterController.add(query));
            c.filterVisible(query);
            SuiteContext.check(graphModel.getGraphVisible().getNodeCount() < nodeCount,
                "The degree filter hides no node");
            c.filterVisible(null);
            SuiteContext.onEdt(() -> c.filterController.remove(query));
            SuiteContext.check(graphModel.getGraphVisible().getNodeCount() == nodeCount,
                "Nodes are still hidden once the filter is removed");
        });

        c.step("overview screenshot",
            () -> SuiteContext.checkNotBlank(c.overviewScreenshot("overview.png"), "The Overview screenshot"));

        c.step("png export", () -> {
            File png = c.file("export.png");
            c.exportController.exportFile(png);
            SuiteContext.checkNotBlank(ImageIO.read(png), "The PNG export");
        });

        c.step("save and reopen", () -> {
            File file = c.file("smoke.gephi");
            c.projectController.saveProject(c.projectController.getCurrentProject(), file);
            c.expectRendering(false);
            c.projectController.closeCurrentProject();
            Project project = c.projectController.openProject(file);
            Graph graph = c.graphController.getGraphModel(project.getCurrentWorkspace()).getGraph();
            SuiteContext.check(graph.getNodeCount() == nodeCount && graph.getEdgeCount() == edgeCount,
                "The reopened project has " + graph.getNodeCount() + " nodes and " + graph.getEdgeCount()
                    + " edges instead of " + nodeCount + " and " + edgeCount);
            c.expectRendering(true);
        });

        c.step("close", () -> {
            c.expectRendering(false);
            c.projectController.closeCurrentProject();
        });
    }
}
