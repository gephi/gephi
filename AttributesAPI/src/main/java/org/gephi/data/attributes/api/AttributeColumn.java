/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.data.attributes.api;

import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;

/**
 * Column is the header of a data column. It belongs to an <code>AttributeTable</code>
 * and is the key to access data within <code>AttributeRow</code>.
 * <p>
 * It contains its index that may be used to get the appropriate value in the
 * <code>AttributeRow</code> values array.
 * <p>
 * For Gephi internal implementation purposes, names of columns are restricted. They can have any name
 * except these defined in {@link org.gephi.data.properties.PropertiesColumn PropertiesColumn} enum.
 * <h2>Iterate rows values</h2>
 * <pre>
 * Attribute row = ...;
 * for(AttributeColumn column : table.getColumns()) {
 *      Object value = row.getValue(column);
 * }
 * </pre>
 * 
 * @author Mathieu Bastian
 * @author Martin Škurla
 * @see AttributeRow
 * @see AttributeTable
 */
public interface AttributeColumn {

    /**
     * Returns the type of this column content.
     *
     * @return  the type of this column
     */
    public AttributeType getType();

    /**
     * Returns the title of this column. The title is a human-readable text that
     * describes the column data. When no title exists, returns the <code>Id</code>
     * of this column.
     *
     * @return  the title of this column, if exists, or the <code>Id</code> otherwise
     */
    public String getTitle();

    /**
     * Returns the index of this column. The index is the fastest way to access a
     * column from its <code>AttributeTable</code> or manipulate
     * <code>AttributeRow</code>.
     * 
     * @return  the index of this column
     * @see     AttributeTable#getColumn(int)
     * @see     AttributeRow#getValue(int)
     */
    public int getIndex();

    /**
     * Returns the origin of this column content, meta-data that describes where
     * the column comes from. Default value is <code>AttributeOrigin.DATA</code>.
     *
     * @return  the origin of this column content
     */
    public AttributeOrigin getOrigin();

    /**
     * Returns the id of this column. The id is the unique identifier that describes
     * the column data.
     *
     * @return  the id of this column
     */
    public String getId();

    /**
     * Returns the default value for this column. May be <code>null</code>.
     * <p>
     * The returned <code>Object</code> class type is equal to the class obtained
     * with <code>AttributeType.getType()</code>.
     *
     * @return  the default value, or <code>null</code>
     */
    public Object getDefaultValue();

    /**
     * Returns the attribute value delegate provider. The Provider is always set if the origin of the
     * current attribute column is AttributeOrigin.DELEGATE.
     *
     * @return attribute value delegate provider
     */
    public AttributeValueDelegateProvider getProvider();
}
