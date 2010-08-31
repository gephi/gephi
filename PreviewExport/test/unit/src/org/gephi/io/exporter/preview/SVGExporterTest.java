/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.exporter.preview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.gephi.desktop.welcome.WelcomeTopComponent;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class SVGExporterTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testExport() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        String sample = "/org/gephi/desktop/welcome/samples/Les Miserables.gexf";
        final InputStream stream = WelcomeTopComponent.class.getResourceAsStream(sample);
        try {
            stream.reset();
        } catch (IOException ex) {
            //Exceptions.printStackTrace(ex);
        }
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FileImporter fileImporter = importController.getFileImporter(".gexf");
        Container container = importController.importFile(stream, fileImporter);

        importController.process(container, new DefaultProcessor(), workspace);

        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();
        model.getNodeSupervisor().setShowNodeLabels(Boolean.TRUE);

        SVGExporter sVGExporter = new SVGExporter();

        sVGExporter.setWorkspace(workspace);
        try {
            File file = new File("test.svg");
            System.out.println(file.getAbsolutePath());
            FileWriter fw = new FileWriter(file);
            sVGExporter.setWriter(fw);
            sVGExporter.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            Exceptions.printStackTrace(ex);
        }
    }
}
