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
package org.gephi.preview.spi;

import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.RenderTarget;

/**
 * Builds and returns new {@link RenderTarget} instances.
 * <p>
 * Render targets are the rendering container and are built by <code>RenderTargetBuilder</code>
 * implementations. Each render target is associated to it's preview model and
 * shouldn't be reused across models. The {@link PreviewModel} provides methods
 * to retrieve properties and dimensions of the graph.
 * <p>
 * Render targets are singleton services and implementations need to add the
 * following annotation to be recognized by the system:
 * <p>
 * <code>@ServiceProvider(service=RenderTargetBuilder.class)</code>
 * @author Mathieu Bastian
 */
public interface RenderTargetBuilder {
    
    /**
     * Builds a new render target using the properties and dimensions defined
     * in <code>previewModel</code>.
     * @param previewModel the preview model to get the dimensions and properties from
     * @return a new render target instance
     */
    public RenderTarget buildRenderTarget(PreviewModel previewModel);
    
    /**
     * Returns the name of the target builder. This value is used by the
     * <code>PreviewController</code> to identify render targets.
     * @return the name of the target builder
     */
    public String getName();
}
