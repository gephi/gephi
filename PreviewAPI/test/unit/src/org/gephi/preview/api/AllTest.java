/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.api;

import java.awt.BorderLayout;
import java.io.InputStream;
import javax.swing.JFrame;
import org.gephi.desktop.welcome.WelcomeTopComponent;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import processing.core.PApplet;

/**
 *
 * @author mbastian
 */
public class AllTest {

    @Test
    public void all() {
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Append container to graph structure
        String sample = "/org/gephi/desktop/welcome/samples/Les Miserables.gexf";
        final InputStream stream = WelcomeTopComponent.class.getResourceAsStream(sample);
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FileImporter fileImporter = importController.getFileImporter(".gexf");
        Container container = importController.importFile(stream, fileImporter);

        importController.process(container, new DefaultProcessor(), workspace);

        //Add self loop
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        Graph graph = graphController.getModel().getGraph();
        Node node = graph.getNode(12);
        System.out.println("Self loop " + node.getNodeData().getLabel());
        graph.addEdge(graphController.getModel().factory().newEdge(node, node, 31, true));

        //Set label edges       
        for (Edge edge : graphController.getModel().getGraph().getEdges()) {
            edge.getEdgeData().setLabel("Label test");
        }

        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        previewController.refreshPreview();

        ProcessingTarget target = (ProcessingTarget) previewController.getRenderTarget(RenderTarget.PROCESSING_TARGET);
        PApplet applet = target.getApplet();
        applet.init();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        previewController.render(target);
        target.refresh();
        target.resetZoom();

        JFrame frame = new JFrame("Test Preview");
        frame.setLayout(new BorderLayout());

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(applet, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);

        try {
            Thread.sleep(100000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
