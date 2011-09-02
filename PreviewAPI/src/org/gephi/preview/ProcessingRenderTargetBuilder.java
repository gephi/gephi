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
package org.gephi.preview;

import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.spi.RenderTargetBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PGraphics2D;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RenderTargetBuilder.class)
public class ProcessingRenderTargetBuilder implements RenderTargetBuilder {

    @Override
    public RenderTarget buildRenderTarget(PreviewModel previewModel) {
        int width = 500;
        int height = 500;
        if (previewModel.getDimensions() != null) {
            width = (int) previewModel.getDimensions().getWidth();
            height = (int) previewModel.getDimensions().getHeight();
        }

        width = Math.max(1, width);
        height = Math.max(1, height);
        return new ProcessingTargetImpl(width, height);
    }

    @Override
    public String getName() {
        return RenderTarget.PROCESSING_TARGET;
    }

    public static class ProcessingTargetImpl implements ProcessingTarget {

        private final PreviewController previewController;
        private final ProcessingApplet applet;
        private final PGraphics graphics;

        public ProcessingTargetImpl(int width, int height) {
            if (System.getProperty("java.awt.headless") != null && System.getProperty("java.awt.headless").equals("true")) {
                //Headless mode
                graphics = new PGraphics2D();
                graphics.setSize(width, height);
                applet = null;
            } else {
                applet = new ProcessingApplet();
                graphics = null;
            }
            previewController = Lookup.getDefault().lookup(PreviewController.class);
        }

        @Override
        public PGraphics getGraphics() {
            if (applet != null) {
                return applet.g;
            }
            return graphics;
        }

        @Override
        public PApplet getApplet() {
            return applet;
        }

        @Override
        public void resetZoom() {
            applet.resetZoom();
        }

        @Override
        public void refresh() {
            applet.refresh(previewController.getModel(), this);
        }

        @Override
        public boolean isRedrawn() {
            return applet.isRedrawn();
        }
    }
}
