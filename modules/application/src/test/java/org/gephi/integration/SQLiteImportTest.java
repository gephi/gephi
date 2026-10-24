package org.gephi.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.io.database.drivers.SQLiteDriver;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.Project;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SQLiteImportTest extends AbstractIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testImport() throws IOException {
        Project project = projectController.newProject();
        // Work on a copy, SQLite may write next to the database file
        File file = new File(tempFolder.getRoot(), "lesmiserables.sqlite3");
        Files.copy(fixture("lesmiserables.sqlite3").toPath(), file.toPath());

        EdgeListDatabaseImpl db = new EdgeListDatabaseImpl();
        db.setHost(file.getAbsolutePath());
        db.setDBName("");
        db.setSQLDriver(new SQLiteDriver());
        db.setNodeQuery("SELECT nodes.id AS id, nodes.label AS label FROM nodes");
        db.setEdgeQuery(
            "SELECT edges.source AS source, edges.target AS target, edges.label AS label, edges.weight AS weight FROM edges");
        Container container = importController.importDatabase(db, new ImporterEdgeList());
        container.getLoader().setAllowAutoNode(false);
        container.getLoader().setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        importController.process(container, new DefaultProcessor(), project.getCurrentWorkspace());

        UndirectedGraph graph = graphController.getGraphModel(project.getCurrentWorkspace()).getUndirectedGraph();
        Assert.assertEquals(77, graph.getNodeCount());
        Assert.assertEquals(254, graph.getEdgeCount());
    }
}
