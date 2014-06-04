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
package org.gephi.data.attributes;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeRowFactory;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.api.AttributeValueFactory;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.NodeData;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeFactoryImpl implements AttributeValueFactory, AttributeRowFactory {

    private AbstractAttributeModel model;

    public AttributeFactoryImpl(AbstractAttributeModel model) {
        this.model = model;
    }

    public AttributeValue newValue(AttributeColumn column, Object value) {
        if (value == null) {
            return new AttributeValueImpl((AttributeColumnImpl) column, null);
        }
        
        //If the column is not a delegate (wrong type value allowed), try to convert value to correct type if necessary
        if(!column.getOrigin().equals(AttributeOrigin.DELEGATE)){
            AttributeType targetType = column.getType();
            if (!value.getClass().equals(targetType.getType())) {
                try {
                    value = targetType.parse(value.toString());//Try to convert to target type
                } catch (Exception ex) {
                    return new AttributeValueImpl((AttributeColumnImpl) column, null);//Could not parse
                }
            }
        }

        
        Object managedValue = value;
        if (!column.getOrigin().equals(AttributeOrigin.PROPERTY)) {
            managedValue = model.getManagedValue(value, column.getType());
        }
        return new AttributeValueImpl((AttributeColumnImpl) column, managedValue);
    }

    public AttributeRowImpl newNodeRow(NodeData nodeData) {
        return new AttributeRowImpl(model.getNodeTable(), nodeData);
    }

    public AttributeRowImpl newEdgeRow(EdgeData edgeData) {
        return new AttributeRowImpl(model.getEdgeTable(), edgeData);
    }

    public AttributeRow newGraphRow(GraphView graphView) {
        return new AttributeRowImpl(model.getGraphTable(), graphView);
    }

    public AttributeRowImpl newRowForTable(String tableName, Object object) {
        AttributeTableImpl attTable = model.getTable(tableName);
        if (attTable != null) {
            return new AttributeRowImpl(attTable, object);
        }
        return null;
    }

    public void setModel(AbstractAttributeModel model) {
        this.model = model;
    }
}
