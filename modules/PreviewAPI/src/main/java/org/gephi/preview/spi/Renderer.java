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
package org.gephi.preview.spi;

import org.gephi.preview.api.*;

/**
 * Renderer describes how a particular {@link Item} object is rendered on a particular
 * {@link RenderTarget} instance.
 * <p>
 * Renderers are the most essential parts of the Preview as they contain the code
 * that actually draws the item on the canvas. Each item (e.g. node, edge) should
 * have its renderer.
 * <p>
 * Rendering is a three-steps process:
 * <ol><li>First the <code>preProcess()</code> method is called on all renderers
 * to let them initialize additional attributes for their items. The best example
 * is the edge renderer which will initialize the source and target position in the
 * <code>EdgeItem</code> object during this phase. In general the <code>preProcess()</code>
 * method is the best for complex algorithms or gathering data from other items. Note that
 * the <code>preProcess()</code> method is called only once per refresh, unlike
 * <code>render()</code> which is called many times.</li>
 * <li>The <code>isRendererForitem()</code> is then used to determine which renderer
 * should be used to render an item. The method provides an access to the preview
 * properties. For instance, if the properties says the edge display is disabled,
 * the edge renderer should return <code>false</code> for every item. Note that
 * nothing avoids several renderer to returns <code>true</code> for the same item.</li>
 * <li>The <code>render()</code> method is finally called for every item which
 * the renderer returned <code>true</code> at <code>isRendererForitem()</code>.
 * It receives the properties and the render target. It uses the item attributes
 * and properties to determine item aspects and the render target to obtain the
 * canvas.</li></ol>
 * <p>
 * Renderers also provide a list of {@link PreviewProperty} which the user can
 * edit. All properties are put in the central {@link PreviewProperties} so though
 * each renderer defines it's properties it can read/write any property through
 * <code>PreviewProperties</code>.
 * <p>
 * If your plugin renderer extends one of the default renderers,
 * your plugin renderer will automatically replace the extended renderer.
 * This means the default renderer will not even be available in the renderers manager.
 * <p>
 * Also, if more than one plugin extends the same default renderer, the one with lowest position
 * will be enabled by the default, but others will still be available for activation in the renderers manager.
 * <p>
 * The list of default renderers is the following (contained in Preview Plugin module);
 * <ol>
 * <li>org.gephi.preview.plugin.renderers.ArrowRenderer</li>
 * <li>org.gephi.preview.plugin.renderers.EdgeLabelRenderer</li>
 * <li>org.gephi.preview.plugin.renderers.EdgeRenderer</li>
 * <li>org.gephi.preview.plugin.renderers.NodeLabelRenderer</li>
 * <li>org.gephi.preview.plugin.renderers.NodeRenderer</li>
 * </ol>
 * <p>
 * Renderers are singleton services and implementations need to add the
 * following annotation to be recognized by the system:
 * <p>
 * <code>@ServiceProvider(service=Renderer.class, position=XXX)</code>
 * <b>Position parameter optional but recommended</b> in order to control the default order in which the available renderers are executed.
 * @author Yudi Xue, Mathieu Bastian
 */
public interface Renderer {

    /**
     * Provides an user friendly name for the renderer.
     * This name will appear in the renderers manager UI.
     * @return User friendly renderer name, not null
     */
    String getDisplayName();

    /**
     * This method is called before rendering for all renderers and initializes
     * items' additional attributes or run complex algorithms.
     * <p>
     * This method has access to any item using the <code>getItems()</code> methods
     * of the preview model.
     * <p>
     * No data should be stored in the renderer itself but put in items using
     * {@link Item#setData(java.lang.String, java.lang.Object)}. Global states can
     * be stored in properties using
     * {@link PreviewProperties#putValue(java.lang.String, java.lang.Object)}.
     * @param previewModel the model to get items from
     */
    void preProcess(PreviewModel previewModel);

    /**
     * Render <code>item</code> to <code>target</code> using the global properties
     * and item data.
     * <p>
     * The target can be one of the default target {@link G2DTarget},
     * {@link SVGTarget} or {@link PDFTarget}. Each target contains an access to
     * it's drawing canvas so the renderer can draw visual items.
     * @param item the item to be rendered
     * @param target the target to render the item on
     * @param properties the central properties
     */
    void render(Item item, RenderTarget target, PreviewProperties properties);

    /**
     * Returns all associated properties for this renderer. Properties can be built
     * using static <code>PreviewProperty.createProperty()</code> methods.
     *
     * @return a properties array
     */
    PreviewProperty[] getProperties();

    /**
     * Based on <code>properties</code>, determine whether this renderer is
     * valid to render <code>Item</code>.
     * <p>
     * Additional states in <code>properties</code> helps to make a decision,
     * including:
     * <ul>
     * <li><b>PreviewProperty.DIRECTED:</b> If the graph is directed</li>
     * <li><b>PreviewProperty.MOVING:</b> Specific to the Processing target, this
     * is <code>true</code> if the user is currently moving the canvas. Renderers
     * other than the node renderer usually render nothing while the user is moving
     * to speeds things up.</li></ul>
     * @param item the item to be tested
     * @param properties the current properties
     * @return <code>true</code> if <code>item</code> can be rendered by this
     * renderer, <code>false</code> otherwise
     */
    boolean isRendererForitem(Item item, PreviewProperties properties);

    /**
     * Based on the <code>itemBuilder</code> class and the <code>properties</code>,
     * determine whether this renderer needs the given <code>itemBuilder</code> to be
     * executed before rendering.
     * <p>
     * This is used for <b>avoiding building unnecessary items</b> while refreshing preview.
     * <p>
     * You can simply return true if the builder builds items that this renderer renders,
     * but you can also check the current properties to see if your renderer is going to produce any graphic.
     * <p>
     *
     * Additional states in <code>properties</code> helps to make a decision,
     * including:
     * <ul>
     * <li><b>PreviewProperty.DIRECTED:</b> If the graph is directed</li>
     * <li><b>PreviewProperty.MOVING:</b> Specific to the Processing target, this
     * is <code>true</code> if the user is currently moving the canvas. Renderers
     * other than the node renderer usually render nothing while the user is moving
     * to speeds things up.</li></ul>
     * @param itemBuilder builder that your renderer may need
     * @param properties the current properties
     * @return <code>true</code> if you are going to use built items for rendering, <code>false</code> otherwise
     */
    boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties);

    /**
     * Compute the canvas size of the item to render.
     *
     * The returned <code>CanvasSize</code> has to embed the whole item to
     * render. If the canvas size cannot be computed, a <code>CanvasSize</code>
     * with both width and height equlal to zero is returned.
     *
     * @param item the item to get the canvas size
     * @param properties the current properties
     * @return the item canvas size
     */
    CanvasSize getCanvasSize(Item item, PreviewProperties properties);
}
