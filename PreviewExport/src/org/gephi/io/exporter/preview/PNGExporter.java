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
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import processing.core.PGraphicsJava2D;

/**
 * 
 * @author Mathieu Bastian
 */
public class PNGExporter implements VectorExporter, ByteExporter, LongTask {
    
    private ProgressTicket progress;
    private boolean cancel = false;
    private Workspace workspace;
    private OutputStream stream;
    private int width = 1024;
    private int height = 1024;
    private boolean transparentBackground = false;
    private int margin = 4;
    private ProcessingTarget target;
    
    @Override
    public boolean execute() {
        Progress.start(progress);
        
        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);
        controller.refreshPreview(workspace);
        
        PreviewProperties props = controller.getModel(workspace).getProperties();
        props.putValue("width", width);
        props.putValue("height", height);
        if (transparentBackground) {
            props.putValue(PreviewProperty.BACKGROUND_COLOR, null);
        }
        props.putValue(PreviewProperty.MARGIN, new Float((float) margin));
        target = (ProcessingTarget) controller.getRenderTarget(RenderTarget.PROCESSING_TARGET, workspace);
        if (target instanceof LongTask) {
            ((LongTask) target).setProgressTicket(progress);
        }
        
        try {
            target.refresh();
            
            Progress.switchToIndeterminate(progress);
            
            PGraphicsJava2D pg2 = (PGraphicsJava2D) target.getGraphics();
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            img.setRGB(0, 0, width, height, pg2.pixels, 0, width);
            ImageIO.write(img, "png", stream);
            stream.close();
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        Progress.finish(progress);
        
        return !cancel;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }
    
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
    
    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
    
    @Override
    public void setOutputStream(OutputStream stream) {
        this.stream = stream;
    }
    
    public boolean cancel() {
        cancel = true;
        if (target instanceof LongTask) {
            ((LongTask) target).cancel();
        }
        return true;
    }
    
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }
}
