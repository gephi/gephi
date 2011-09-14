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

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RenderTargetBuilder.class)
public class ProcessingRenderTargetBuilder implements RenderTargetBuilder {

    @Override
    public RenderTarget buildRenderTarget(PreviewModel previewModel) {
        Integer width = previewModel.getProperties().getValue("width");
        Integer height = previewModel.getProperties().getValue("height");
        if (width != null && height != null) {
            //Headless  mode
            width = Math.max(1, width);
            height = Math.max(1, height);
            return new ProcessingTargetImpl(width, height);
        } else {
            //Applet mode
            return new ProcessingTargetImpl();
        }
    }

    @Override
    public String getName() {
        return RenderTarget.PROCESSING_TARGET;
    }

    public static class ProcessingTargetImpl extends AbstractRenderTarget implements ProcessingTarget {

        private final PreviewController previewController;
        private final ProcessingApplet applet;
        private final ProcessingGraphics graphics;

        public ProcessingTargetImpl() {
            applet = new ProcessingApplet();
            graphics = null;
            previewController = Lookup.getDefault().lookup(PreviewController.class);
        }

        public ProcessingTargetImpl(int width, int height) {
            graphics = new ProcessingGraphics(width, height);
            applet = null;
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
            if (applet != null) {
                applet.resetZoom();
            }
        }

        @Override
        public void refresh() {
            if (applet != null) {
                applet.refresh(previewController.getModel(), this);
            } else if (graphics != null) {
                graphics.refresh(previewController.getModel(), this);
            }
        }

        @Override
        public boolean isRedrawn() {
            if (applet != null) {
                return applet.isRedrawn();
            }
            return true;
        }

        @Override
        public void zoomPlus() {
            if (applet != null) {
                applet.zoomPlus();
            }
        }

        @Override
        public void zoomMinus() {
            if (applet != null) {
                applet.zoomMinus();
            }
        }
    }
}
