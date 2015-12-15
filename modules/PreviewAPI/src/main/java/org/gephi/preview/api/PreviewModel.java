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

import java.awt.Dimension;
import java.awt.Point;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.PreviewMouseListener;
import org.gephi.preview.spi.Renderer;

/**
 * The Preview Model contains all items and all preview properties. <p> Items are the visual elements built from the {@link Graph} by {@link ItemBuilder} implementations and can be retrieved from this
 * class. Each item has a type and default types are {@link Item#NODE}, {@link Item#EDGE}, {@link Item#NODE_LABEL} and {@link Item#EDGE_LABEL}. <p> A preview model is attached to it's workspace and
 * can be retrieved from the
 * {@link PreviewController}.
 *
 * @author Yudi Xue, Mathieu Bastian
 * @see Item
 * @see Renderer
 */
public interface PreviewModel {

    /**
     * Returns the preview properties attached to this model.
     *
     * @return the preview properties
     */
    PreviewProperties getProperties();

    /**
     * Returns all items with
     * <code>type</code> as type. <p> Default types are {@link Item#NODE}, {@link Item#EDGE}, {@link Item#NODE_LABEL} and {@link Item#EDGE_LABEL}.
     *
     * @param type the item's type
     * @return all items from this type
     */
    Item[] getItems(String type);

    /**
     * Returns all items attached to
     * <code>source</code>. <p> The source is the graph object behind the item (e.g.
     * {@link Node} or {@link Edge}). Multiple items can be created from the same source object. For instance both
     * <code>Item.NODE</code> and
     * <code>Item.NODE_LABEL</code> have the node object as source.
     *
     * @param source the item's source
     * @return all items with
     * <code>source</code> as source
     */
    Item[] getItems(Object source);

    /**
     * Returns the item attached to
     * <code>source</code> and with the type
     * <code>type</code>. <p> The source is the graph object behind the item (e.g.
     * {@link Node} or {@link Edge}) and the type a default or a custom type. <p> Default types are {@link Item#NODE}, {@link Item#EDGE}, {@link Item#NODE_LABEL} and {@link Item#EDGE_LABEL}.
     *
     * @param type the item's type
     * @param source the item's source object
     * @return the item or
     * <code>null</code> if not found
     */
    Item getItem(String type, Object source);

    /**
     * <p>Returns currently managed renderers, or null.</p> <p>If
     * <code>managedRenderers</code> is set to null, all renderers will be executed when rendering, in default implementation order.</p>
     *
     * @return Enabled renderers or null
     */
    ManagedRenderer[] getManagedRenderers();

    /**
     * <p>Sets an user-defined array of managed renderers to use when rendering.</p> <p><b>Only</b> the renderers marked as enabled will be executed when rendering, and <b>respecting the array
     * order</b></p> <p>If the input array does not contain a managed renderer for some renderer existing implementation, a new not enabled managed renderer will be added to the end of the input
     * array</p> <p>If
     * <code>managedRenderers</code> is set to null, all renderers will be executed when rendering, in default implementation order.</p>
     *
     * @param managedRenderers Managed renderers for future renderings
     */
    void setManagedRenderers(ManagedRenderer[] managedRenderers);

    /**
     * Returns
     * <code>managedRenderers</code> Renderers that are enabled, or null if
     * <code>managedRenderers</code> is null.
     *
     * @return Enabled renderers or null
     */
    Renderer[] getManagedEnabledRenderers();

    /*
     * Returns <code>managedPreviewMouseListeners</code> containing the <code>PreviewMouseListeners</code> that are declared by the current enabled managed renderers.
     */
    PreviewMouseListener[] getEnabledMouseListeners();

    /**
     * Returns the width and height of the graph in the graph coordinates.
     *
     * @return the graph dimensions
     */
    Dimension getDimensions();

    /**
     * Returns the top left position in the graph coordinate (i.e. not the preview coordinates).
     *
     * @return the top left position point
     */
    Point getTopLeftPosition();
}
