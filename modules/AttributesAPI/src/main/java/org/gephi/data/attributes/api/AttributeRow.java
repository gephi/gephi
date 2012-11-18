/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Cezary Bartosiak
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

import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.NodeData;

/**
 * Rows contains {@link AttributeValue}, one for each column. Rows are
 * not stored in columns and nor in tables, they are stored in the object that
 * possess the row, for instance <code>Nodes</code> or <code>Edges</code>.
 * <p>
 * But colums are fixed, stored in <code>AttributeTable</code>. Rows always
 * contains the values in the same order as columns are described in the table.
 * <p>
 * For instance, if an table contains a single column <b>label</b>, the column
 * index is equal to <b>0</b> and the value can be retrieved in the following ways:
 * <ul>
 * <li><code>row.getValue(column);</code></li>
 * <li><code>row.getValue("label");</code></li>
 * <li><code>row.getValue(0);</code></li>
 * </ul>
 * Rows are build from a {@link AttributeRowFactory}, that can be get from the
 * {@link AttributeModel}.
 * <h3>Nodes and edges</h3>
 * Nodes and edges elements are build from <b>Graph API</b>, and already have a
 * default row that can be found with {@link NodeData#getAttributes()} and
 * {@link EdgeData#getAttributes()}. Please cast <code>Attributes</code> in
 * <code>AttributesRow</code> to profit from the complete API.
 *
 * @author Mathieu Bastian
 * @author Cezary Bartosiak
 * @see AttributeColumn
 * @see AttributeTable
 * @see AttributeValue
 */
public interface AttributeRow extends Attributes {

    /**
     * Resets all data in the row.
     */
    public void reset();

    /**
     * Returns the number of values this rows contains. Equal to the number of
     * columns of the <code>AttributeTable</code> this row belongs.
     * 
     * @return          the size of the values array
     */
    public int countValues();

    /**
     * Sets values from another row. Values must have existing column in the
     * current table.
     *
     * @param row       an existing row that may refer to the same columns
     */
    public void setValues(AttributeRow row);

    /**
     * Sets a value for this row. If the <code>column</code> retrieved from
     * <code>value</code> cannot be found at the same index, the column
     * <code>Id</code> is used to find the column.
     *
     * @param value     a value that refers to an existing column for this row
     */
    public void setValue(AttributeValue value);

    /**
     * Sets a value at the specified column index.
     *
     * @param column    a column that exists for this row
     * @param value     the value that is to be set a the specified column index
     */
    public void setValue(AttributeColumn column, Object value);

    /**
     * Sets a value at the specified column index, if column is found. The
     * column is found if <code>column</code> refers to an existing column
     * <code>id</code> or <code>title</code>.
     *
     * @param column    a column <code>id</code> or <code>title</code>
     * @param value     the value that is to be set if <code>column</code> is found
     */
    public void setValue(String column, Object value);

    /**
     * Sets a value at the specified column index, if <code>index</code> is in
     * range. This is equivalent as
     * <code>setValue(AttributeColumn.getIndex(), Object)</code>.
     *
     * @param index     a valid column index
     * @param value     the value that is to be set if <code>index</code> is valide
     */
    public void setValue(int index, Object value);

    /**
     * Returns the value found at the specified column index. May return
     * <code>null</code> if the value is <code>null</code> or if the column
     * doesn't exist.
     * 
     * @param column    a column that exists for this row
     * @return          the value found at the specified column index or
     *                  <code>null</code> otherwise
     */
    public Object getValue(AttributeColumn column);

    /**
     * Returns the value at the specified column, if found. The
     * column is found if <code>column</code> refers to an existing column
     * <code>id</code> or <code>title</code>.
     *
     * @param column    a column <code>id</code> or <code>title</code>
     * @return          the value found at the specified column or
     *                  <code>null</code> otherwise
     */
    public Object getValue(String column);

    /**
     * Returns the value at the specified index, if <code>index</code> is in range.
     * This is equivalent as <code>getValue(AttributeColumn.getIndex())</code>.
     *
     * @param index     a valid column index
     * @return          the value found at the specified column or
     *                  <code>null</code> otherwise
     * @see             AttributeColumn#getIndex()
     */
    public Object getValue(int index);

    /**
     * Returns the value array. Each <code>AttributeValue</code> is a pair between
     * a data and the column it belongs.
     *
     * @return          the value array of this row
     */
    public AttributeValue[] getValues();

    /**
     * Returns the value at given index or null if the index is not valid. Each <code>AttributeValue</code> is a pair between
     * a data and the column it belongs.
     * @param index
     * @return AttributeValue at given index or null if the index is not valid
     */
    public AttributeValue getAttributeValueAt(int index);

    /**
     * Returns the column at given index or null if the index is not valid
     * @param index
     * @return AttributeColumn at given index or null if the index is not valid
     */
    public AttributeColumn getColumnAt(int index);
}
