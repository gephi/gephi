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
package org.gephi.io.exporter.preview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.gephi.desktop.welcome.WelcomeTopComponent;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Test;
import org.openide.util.Lookup;
import processing.core.PGraphicsJava2D;

/**
 *
 * @author mbastian
 */
public class PNGExporterTest {

    @Test
    public void testPNG() {

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

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.refreshPreview(workspace);
        PreviewProperties props = controller.getModel(workspace).getProperties();
        props.putValue("width", 1300);
        props.putValue("height", 1000);
        props.putValue(PreviewProperty.BACKGROUND_COLOR, null);
        ProcessingTarget target = (ProcessingTarget) controller.getRenderTarget(RenderTarget.PROCESSING_TARGET, workspace);

        try {
            File file = new File("/Users/mbastian/test.png");
            System.out.println(file.getAbsolutePath());
            PGraphicsJava2D pg2 = (PGraphicsJava2D) target.getGraphics();
            target.refresh();

            BufferedImage img = new BufferedImage(pg2.width, pg2.height, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, pg2.width, pg2.height, pg2.pixels, 0, pg2.width);
            FileOutputStream fos = new FileOutputStream(file);
            ImageIO.write(img, "png", fos);
            fos.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
