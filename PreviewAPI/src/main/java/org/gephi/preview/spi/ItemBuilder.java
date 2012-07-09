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

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;

/**
 * Builds and returns new {@link Item} instances.
 * <p>
 * Items are the visual elements representing the graph and are built by item
 * builders from the graph object. 
 * <p>
 * An item builder should build only a single type of items. Items are defined 
 * by a type {@link Item#getType()}, which needs to be the same as the value
 * returned by {@link #getType()}. In other words if this builder is building
 * <code>Node.Item</code> items, it should return <code>Node.Item</code> as a type.
 * <p>
 * Item builders are singleton services and implementations need to add the
 * following annotation to be recognized by the system:
 * <p>
 * <code>@ServiceProvider(service=ItemBuilder.class)</code>
 * 
 * @author Yudi Xue, Mathieu Bastian
 */
public interface ItemBuilder {

    public static final String NODE_BUILDER = Item.NODE;
    public static final String NODE_LABEL_BUILDER = Item.NODE_LABEL;
    public static final String EDGE_BUILDER = Item.EDGE;
    public static final String EDGE_LABEL_BUILDER = Item.EDGE_LABEL;

    /**
     * Build items from the <code>graph</code> and <code>attributeModel</code>.
     * @param graph the graph to build items from
     * @param attributeModel the attribute model associated to the graph
     * @return an array of new items, from the same type returned by {@link #getType()}
     */
    public Item[] getItems(Graph graph, AttributeModel attributeModel);

    /**
     * Returns the type of this builder. 
     * <p>
     * The type should <b>always</b> match
     * the type of <code>Item</code> the builder is building. For instance if the
     * builder is building <code>Item.Node</code> type, this method should return
     * <code>Item.Node</code>.
     * @return the builder item type.
     */
    public String getType();
}
