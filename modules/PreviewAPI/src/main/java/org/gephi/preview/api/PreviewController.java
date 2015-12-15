/*
Copyright 2008-2011 Gephi
Authors : Yudi Xue <yudi.xue@usask.ca>, Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
    void refreshPreview(Workspace workspace);

    /**
     * Refreshes the current preview model. 
     * <p>
     * This task built all items from <code>ItemBuilder</code> implementations,
     * refresh graph dimensions and call all <code>Renderer.preProcess()</code>
     * method.
     */
    void refreshPreview();

    /**
     * Returns the current preview model in the current workspace.
     * @return the current preview model
     */
    PreviewModel getModel();

    /**
     * Returns the preview model in <code>workspace</code>.
     * @param workspace the workspace to lookup
     * @return the preview model in <code>workspace</code>
     */
    PreviewModel getModel(Workspace workspace);

    /**
     * Renders the current preview model to <code>target</code>.
     * <p>
     * If preview model <code>managedRenderers</code> is null, this task looks for all <code>Renderer</code> implementations in their default order.
     * Then all items in the preview model are rendered.
     * @param target the target to render items to
     */
    void render(RenderTarget target);

    /**
     * Renders the preview model in <code>workspace</code> to <code>target</code>.
     * <p>
     * If preview model <code>managedRenderers</code> is null, this task looks for all <code>Renderer</code> implementations in their default order.
     * Then all items in the preview model are rendered.
     * @param target the target to render items to
     * @param workspace the workspace to get the preview model from
     */
    void render(RenderTarget target, Workspace workspace);
    
    /**
     * Renders the current preview model to <code>target</code>.
     * <p>
     * This task overrides the preview model <code>managedRenderers</code> and uses the given <code>Renderer</code> array, <b>respecting the array order</b>.
     * Then all items in the preview model are rendered.
     * @param target the target to render items to
     * @param renderers renderers to use
     */
    void render(RenderTarget target, Renderer[] renderers);
    
    /**
     * Renders the preview model in <code>workspace</code> to <code>target</code>.
     * <p>
     * This task overrides the preview model <code>managedRenderers</code> and uses the given <code>Renderer</code> array, <b>respecting the array order</b>.
     * Then all items in the preview model are rendered.
     * @param target the target to render items to
     * @param renderers renderers to use
     * @param workspace the workspace to get the preview model from
     */
    void render(RenderTarget target, Renderer[] renderers, Workspace workspace);

    /**
     * Creates a new render target of the given type. 
     * <p>
     * Default render targets names are {@link RenderTarget#G2D_TARGET},
     * {@link RenderTarget#SVG_TARGET} and {@link RenderTarget#PDF_TARGET}.
     * <p>
     * Render targets usually need some parameters when built. Parameters values
     * should simply be put in the <code>PreviewProperties</code>.
     * @param name the name of the render target
     * @return a new render target or <code>null</code> if <code>name</code> is
     * unknown
     */
    RenderTarget getRenderTarget(String name);

    /**
     * Creates a new render target of the given type in the preview model
     * contained by <code>workspace</code>.
     * <p>
     * Default render targets names are {@link RenderTarget#G2D_TARGET},
     * {@link RenderTarget#SVG_TARGET} and {@link RenderTarget#PDF_TARGET}.
     * <p>
     * Render targets usually need some parameters when built. Parameters values
     * should simply be put in the <code>PreviewProperties</code>.
     * @param name the name of the render target
     * @param workspace the workspace to get the preview model from
     * @return a new render target or <code>null</code> if <code>name</code> is
     * unknown
     */
    RenderTarget getRenderTarget(String name, Workspace workspace);
    
    /**
     * Uses <code>Lookup</code> to retrieve registered renderer providers but replaces default renderers with plugins that extend them.
     * @see Renderer
     * @return Registered renderers replacing default renderers with their extension plugins in case they exist
     */
    Renderer[] getRegisteredRenderers();
    
    /**
     * Returns true if any renderer plugin is registered.
     * @return True if any plugin renderer is found in the system
     */
    boolean isAnyPluginRendererRegistered();
    
    /**
     * Sends a <code>PreviewMouseEvent</code> to the current workspace, if any.
     * @param event PreviewMouseEvent
     * @return True if the event was consumed, false otherwise
     */
    boolean sendMouseEvent(PreviewMouseEvent event);
    
    /**
     * Sends a <code>PreviewMouseEvent</code> to the given workspace.
     * @param event PreviewMouseEvent
     * @param workspace workspace
     * @return True if the event was consumed, false otherwise
     */
    boolean sendMouseEvent(PreviewMouseEvent event, Workspace workspace);
}
