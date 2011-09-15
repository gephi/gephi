/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
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

import org.gephi.preview.spi.Renderer;
import org.gephi.project.api.Workspace;

/**
 * Controller that maintain the preview models, one per workspace.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>PreviewController gc = Lookup.getDefault().lookup(PreviewController.class);</pre>

 * @author Yudi Xue, Mathieu Bastian
 * @see PreviewModel
 * @see Item
 * @see Renderer
 */
public interface PreviewController {

    /**
     * Refreshes the preview model in <code>workspace</code>.
     * <p>
     * This task built all items from <code>ItemBuilder</code> implementations,
     * refresh graph dimensions and call all <code>Renderer.preProcess()</code>
     * method.
     * @param workspace the workspace to get the preview model from
     */
    public void refreshPreview(Workspace workspace);

    /**
     * Refreshes the current preview model. 
     * <p>
     * This task built all items from <code>ItemBuilder</code> implementations,
     * refresh graph dimensions and call all <code>Renderer.preProcess()</code>
     * method.
     */
    public void refreshPreview();

    /**
     * Returns the current preview model in the current workspace.
     * @return the current preview model
     */
    public PreviewModel getModel();

    /**
     * Returns the preview model in <code>workspace</code>.
     * @param workspace the workspace to lookup
     * @return the preview model in <code>workspace</code>
     */
    public PreviewModel getModel(Workspace workspace);

    /**
     * Renders the current preview model to <code>target</code>.
     * <p>
     * This tasks look for all <code>Renderer</code> implementations and render
     * all items in the preview model.
     * @param target the target to render items to
     */
    public void render(RenderTarget target);

    /**
     * Renders the preview model in <code>workspace</code> to <code>target</code>.
     * <p>
     * This tasks look for all <code>Renderer</code> implementations and render
     * all items in the preview model.
     * @param target the target to render items to
     * @param workspace the workspace to get the preview model from
     */
    public void render(RenderTarget target, Workspace workspace);

    /**
     * Creates a new render target of the given type. 
     * <p>
     * Default render targets names are {@link RenderTarget#PROCESSING_TARGET},
     * {@link RenderTarget#SVG_TARGET} and {@link RenderTarget#PDF_TARGET}.
     * <p>
     * Render targets usually need some parameters when built. Parameters values
     * should simply be put in the <code>PreviewProperties</code>.
     * @param name the name of the render target
     * @return a new render target or <code>null</code> if <code>name</code> is
     * unknown
     */
    public RenderTarget getRenderTarget(String name);

    /**
     * Creates a new render target of the given type in the preview model
     * contained by <code>workspace</code>.
     * <p>
     * Default render targets names are {@link RenderTarget#PROCESSING_TARGET},
     * {@link RenderTarget#SVG_TARGET} and {@link RenderTarget#PDF_TARGET}.
     * <p>
     * Render targets usually need some parameters when built. Parameters values
     * should simply be put in the <code>PreviewProperties</code>.
     * @param name the name of the render target
     * @param workspace the workspace to get the preview model from
     * @return a new render target or <code>null</code> if <code>name</code> is
     * unknown
     */
    public RenderTarget getRenderTarget(String name, Workspace workspace);
}
