package org.gephi.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.GraphController;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.api.LayoutController;
import org.gephi.preview.api.PreviewController;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.Assert;
import org.openide.util.Lookup;

/**
 * Base class for tests that drive Gephi through its controllers, with every Gephi module on the classpath. Each test
 * starts without a project and the current project is closed afterwards.
 */
public abstract class AbstractIntegrationTest {

    protected final ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);

    protected final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

    protected final AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);

    protected final FilterController filterController = Lookup.getDefault().lookup(FilterController.class);

    protected final ImportController importController = Lookup.getDefault().lookup(ImportController.class);

    protected final ExportController exportController = Lookup.getDefault().lookup(ExportController.class);

    protected final PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);

    protected final LayoutController layoutController = Lookup.getDefault().lookup(LayoutController.class);

    @After
    public void closeProject() {
        projectController.closeCurrentProject();
    }

    /**
     * Returns a file from this package's test resources.
     */
    protected File fixture(String name) throws FileNotFoundException {
        URL url = AbstractIntegrationTest.class.getResource(name);
        if (url == null) {
            throw new FileNotFoundException(name);
        }
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Imports <code>file</code> into a new workspace of the current project and selects it. Auto scaling is disabled
     * so that positions are kept as they are in the file.
     */
    protected Workspace importFile(File file) throws FileNotFoundException {
        Container container = importController.importFile(file);
        Assert.assertNotNull("No importer for " + file, container);
        return process(container);
    }

    /**
     * Processes <code>container</code> into a new workspace of the current project and selects it. Auto scaling is
     * disabled so that positions are kept as they were imported.
     */
    protected Workspace process(Container container) {
        container.getLoader().setAutoScale(false);
        if (projectController.getCurrentProject() == null) {
            projectController.newProject();
        }
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        importController.process(container, new DefaultProcessor(), workspace);
        projectController.openWorkspace(workspace);
        return workspace;
    }
}
