package org.gephi.preview.api;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFrame;
import org.gephi.desktop.welcome.WelcomeTopComponent;
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
        try {
            stream.reset();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        FileImporter fileImporter = importController.getFileImporter(".gexf");
        Container container = importController.importFile(stream, fileImporter);

        importController.process(container, new DefaultProcessor(), workspace);

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
