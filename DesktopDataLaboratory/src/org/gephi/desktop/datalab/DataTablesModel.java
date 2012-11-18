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
package org.gephi.desktop.datalab;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.project.api.Workspace;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class DataTablesModel implements AttributeListener {

    private AvailableColumnsModel nodeAvailableColumnsModel;
    private AvailableColumnsModel edgeAvailableColumnsModel;
    private AttributeModel attributeModel;

    public DataTablesModel(Workspace workspace) {
        nodeAvailableColumnsModel = new AvailableColumnsModel();
        edgeAvailableColumnsModel = new AvailableColumnsModel();

        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);

        //Try to make available all columns at start by default:
        for (AttributeColumn column : attributeModel.getNodeTable().getColumns()) {
            System.out.println("");
            nodeAvailableColumnsModel.addAvailableColumn(column);
        }

        for (AttributeColumn column : attributeModel.getEdgeTable().getColumns()) {
            edgeAvailableColumnsModel.addAvailableColumn(column);
        }

        attributeModel.addAttributeListener(this);
    }

    public AvailableColumnsModel getEdgeAvailableColumnsModel() {
        return edgeAvailableColumnsModel;
    }

    public AvailableColumnsModel getNodeAvailableColumnsModel() {
        return nodeAvailableColumnsModel;
    }
    
    private AvailableColumnsModel getTableAvailableColumnsModel(AttributeTable table) {
        if (attributeModel.getNodeTable() == table) {
            return nodeAvailableColumnsModel;
        } else if (attributeModel.getEdgeTable() == table) {
            return edgeAvailableColumnsModel;
        } else {
            return null;//Graph table or other table, not supported in data laboratory for now.
        }
    }

    public void attributesChanged(AttributeEvent event) {
        AttributeTable table = event.getSource();
        AvailableColumnsModel tableAvailableColumnsModel = getTableAvailableColumnsModel(table);
        if (tableAvailableColumnsModel != null) {
            switch (event.getEventType()) {
                case ADD_COLUMN:
                    for (AttributeColumn c : event.getData().getAddedColumns()) {
                        if (!tableAvailableColumnsModel.addAvailableColumn(c)) {//Add as available by default. Will only be added if the max number of available columns is not surpassed
                            break;
                        }
                    }
                    break;
                case REMOVE_COLUMN:
                    for (AttributeColumn c : event.getData().getRemovedColumns()) {
                        tableAvailableColumnsModel.removeAvailableColumn(c);
                    }
                    break;
                case REPLACE_COLUMN:
                    for (AttributeColumn c : event.getData().getRemovedColumns()) {
                        tableAvailableColumnsModel.removeAvailableColumn(c);
                        tableAvailableColumnsModel.addAvailableColumn(table.getColumn(c.getId()));
                    }
                    break;
            }
        }
        Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
    }
}
