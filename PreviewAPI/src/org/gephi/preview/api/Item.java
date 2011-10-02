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

import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;

/**
 * An item is a visual element built by an {@link ItemBuilder} and later used
 * by a {@link Renderer} to be displayed. 
 * <p>
 * An item simply stores the reference to the original object (e.g. node, edge) and
 * all the information useful for the <code>Renderer</code> like the color, size or
 * position.
 * <p>
 * All items can be retrieved from the {@link PreviewModel}.
 * 
 * @author Yudi Xue, Mathieu Bastian
 */
public interface Item {

    public static final String NODE = "node";
    public static final String EDGE = "edge";
    public static final String NODE_LABEL = "node_label";
    public static final String EDGE_LABEL = "edge_label";

    /**
     * Returns the source of the item. The source is usually a graph object like
     * a <code>Node</code> or <code>Edge</code>.
     * @return the item's source object
     */
    public Object getSource();

    /**
     * Returns the type of the item. Default types are <code>Item.NODE</code>, 
     * <code>Item.EDGE</code>, <code>Item.NODE_LABEL</code> and <code>Item.EDGE_LABEL</code>.
     * @return the item's type
     */
    public String getType();

    /**
     * Returns data associated to this item.
     * @param <D> the type of the data
     * @param key the key
     * @return the value associated to <code>key</code>, or <code>null</code> if
     * not exist
     */
    public <D> D getData(String key);

    /**
     * Sets data to this item.
     * @param key the key
     * @param value the value to be associated with <code>key</code>
     */
    public void setData(String key, Object value);

    /**
     * Returns all the keys. That allows to enumerate all data associated with
     * this item.
     * @return all keys
     */
    public String[] getKeys();
}
