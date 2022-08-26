package org.gephi.io.importer;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

public class GraphImporter {

    public static GraphModel importGraph(Class resourceLocation, String filename) {
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        Processor processor = Lookup.getDefault().lookup(Processor.class);
        if (processor == null) {
            throw new RuntimeException(
                "The import processor can't be found, make sure to add a dependency to the ProcessorPlugin module.");
        }
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        Container container = importContainer(resourceLocation, filename);
        importController.process(container);

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        return graphController.getGraphModel();
    }

    public static Container importContainer(Class resourceLocation, String fileName) {
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        String extension = fileName.substring(fileName.lastIndexOf('.'));
        FileImporter importer = importController.getFileImporter(extension);
        if (importer == null) {
            throw new RuntimeException("The importer for extension '" + extension +
                "' can't be found, make sure to add a dependency to the ImportPlugin module.");
        }
        return importController.importFile(getReader(resourceLocation, fileName), importer);
    }

    private static Reader getReader(Class resourceLocation, String fileName) {
        try {
            String content = new String(resourceLocation.getResourceAsStream(fileName)
                .readAllBytes());
            return new StringReader(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
