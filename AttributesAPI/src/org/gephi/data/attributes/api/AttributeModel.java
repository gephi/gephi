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

/**
 * Represents the data model, like a standard database would do. As a database,
 * contains a list of tables, where columns are defined. By default, a model
 * owns a <b>node</b> and <b>edge</b> table, but more could exist, depending
 * of the model implementation.
 * <p>
 * The model also provides factories that are linked to this model. Use row
 * factory to build new rows and value factory to push new values to these
 * rows. Columns are manipulated from the <code>AttributeTable</code> class.
 *
 * @author Mathieu Bastian
 * @see AttributeController
 */
public interface AttributeModel {

    /**
     * Returns the <b>node</b> table. Contains all the columns associated to
     * node elements.
     * <p>
     * An <code>AttributeModel</code> has always <b>node</b> and <b>edge</b>
     * tables by default.
     *
     * @return      the node table, contains node columns
     */
    public AttributeTable getNodeTable();

    /**
     * Returns the <b>edge</b> table. Contains all the columns associated to
     * edge elements.
     * <p>
     * An <code>AttributeModel</code> has always <b>node</b> and <b>edge</b>
     * tables by default.
     *
     * @return      the edge table, contains edge columns
     */
    public AttributeTable getEdgeTable();

    /**
     * Returns the <code>AttributeTable</code> which has the given <code>name</code>
     * or <code>null</code> if this table doesn't exist.
     *
     * @param name  the table's name
     * @return      the table that has been found, or <code>null</code>
     */
    public AttributeTable getTable(String name);

    /**
     * Returns all tables this model contains. By default, only contains
     * <b>node</b> and <b>edge</b> tables.
     *
     * @return      all the tables of this model
     */
    public AttributeTable[] getTables();

    /**
     * Return the value factory.
     * 
     * @return      the value factory
     */
    public AttributeValueFactory valueFactory();

    /**
     * Returns the row factory.
     *
     * @return      the row factory
     */
    public AttributeRowFactory rowFactory();

    /**
     * Adds <code>listener</code> to the listeners of this table. It receives
     * events when columns are added or removed, as well as when values are set.
     * @param listener      the listener that is to be added
     */
    public void addAttributeListener(AttributeListener listener);

    /**
     * Removes <code>listener</code> to the listeners of this table.
     * @param listener      the listener that is to be removed
     */
    public void removeAttributeListener(AttributeListener listener);

    /**
     * Merge <code>model</code> in this model. Makes the union of tables and
     * columns of both models. Copy tables this model don't
     * have and merge existing ones. For existing tables, call
     * {@link AttributeTable#mergeTable(AttributeTable)}
     * to merge columns.
     * <p>
     * Columns are compared according to their <code>id</code> and <code>type</code>.
     * Columns found in <code>model</code> are appended only if they no column
     * exist with the same <code>id</code> and <code>type</code>.
     *
     * @param model the model that is to be merged in this model
     */
    public void mergeModel(AttributeModel model);
}
