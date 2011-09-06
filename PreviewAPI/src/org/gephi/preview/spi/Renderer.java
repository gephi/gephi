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
package org.gephi.preview.spi;

import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.ProcessingTarget;
import org.gephi.preview.api.SVGTarget;

/**
 * Renderer describes how a particular {@link Item} object is rendered on a particular
 * {@link RenderTarget} instance.
 * <p>
 * Renderers are the most essential parts of the Preview as they contain the code
 * that actually draws the item on the canvas. Each item (e.g. node, edge) should
 * have it's renderer.
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
 * Renderers also provides a list of {@link PreviewProperty} which the user can
 * edit. All properties are put in the central {@link PreviewProperties} so though
 * each renderer defines it's properties it can read/write any property through
 * <code>PreviewProperties</code>.
 * <p>
 * Renderers are singleton services and implementations need to add the
 * following annotation to be recognized by the system:
 * <p>
 * <code>@ServiceProvider(service=Renderer.class)</code>
 * @author Yudi Xue, Mathieu Bastian
 */
public interface Renderer {

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
    public void preProcess(PreviewModel previewModel);

    /**
     * Render <code>item</code> to <code>target</code> using the global properties
     * and item data.
     * <p>
     * The target can be one of the default target {@link ProcessingTarget}, 
     * {@link SVGTarget} or {@link PDFTarget}. Each target contains an access to 
     * it's drawing canvas so the renderer can draw visual items.
     * @param item the item to be rendered
     * @param target the target to render the item on
     * @param properties the central properties
     */
    public void render(Item item, RenderTarget target, PreviewProperties properties);

    /**
     * Returns all associated properties for this renderer. Properties can be built
     * using static <code>PreviewProperty.createProperty()</code> methods. 
     * 
     * @return a properties array
     */
    public PreviewProperty[] getProperties();

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
    public boolean isRendererForitem(Item item, PreviewProperties properties);
}
