/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.desktop.datalab.persistence;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.desktop.datalab.AvailableColumnsModel;
import org.gephi.desktop.datalab.DataTablesModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Eduardo
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 16000)
public class DataLaboratoryPersistenceProvider implements WorkspacePersistenceProvider {

    private static final String AVAILABLE_COLUMNS = "availablecolumns";
    private static final String NODE_COLUMN = "nodecolumn";
    private static final String EDGE_COLUMN = "edgecolumn";

    public Element writeXML(Document document, Workspace workspace) {
        AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        DataTablesModel dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
        if (dataTablesModel == null) {
            workspace.add(dataTablesModel = new DataTablesModel(attributeModel.getNodeTable(), attributeModel.getEdgeTable()));
        }

        return writeDataTablesModel(document, dataTablesModel);
    }

    public void readXML(Element element, Workspace workspace) {
        readDataTablesModel(element, workspace);
    }

    public String getIdentifier() {
        return AVAILABLE_COLUMNS;
    }

    private Element writeDataTablesModel(Document document, DataTablesModel dataTablesModel) {
        Element element = document.createElement(AVAILABLE_COLUMNS);
        Element colE;
        for (AttributeColumn column : dataTablesModel.getNodeAvailableColumnsModel().getAvailableColumns()) {
            colE = document.createElement(NODE_COLUMN);
            colE.setAttribute("id", String.valueOf(column.getIndex()));
            element.appendChild(colE);
        }

        for (AttributeColumn column : dataTablesModel.getEdgeAvailableColumnsModel().getAvailableColumns()) {
            colE = document.createElement(EDGE_COLUMN);
            colE.setAttribute("id", String.valueOf(column.getIndex()));
            element.appendChild(colE);
        }

        return element;
    }

    private void readDataTablesModel(Element element, Workspace workspace) {
        AttributeModel attributeModel=workspace.getLookup().lookup(AttributeModel.class);
        AttributeTable nodesTable=attributeModel.getNodeTable();
        AttributeTable edgesTable=attributeModel.getEdgeTable();
        DataTablesModel dataTablesModel=workspace.getLookup().lookup(DataTablesModel.class);
        if(dataTablesModel==null){
            workspace.add(dataTablesModel = new DataTablesModel());
        }        
        AvailableColumnsModel nodeColumns = dataTablesModel.getNodeAvailableColumnsModel();
        nodeColumns.removeAllColumns();
        AvailableColumnsModel edgeColumns = dataTablesModel.getEdgeAvailableColumnsModel();
        edgeColumns.removeAllColumns();

        NodeList rowList = element.getChildNodes();
        int id;
        AttributeColumn column;
        for (int i = 0; i < rowList.getLength(); i++) {
            if (rowList.item(i).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                Element itemE = (Element) rowList.item(i);
                id = Integer.parseInt(itemE.getAttribute("id"));
                if (itemE.getTagName().equals(NODE_COLUMN)) {
                    column=nodesTable.getColumn(id);
                    if(column!=null){
                        nodeColumns.addAvailableColumn(column);
                    }
                } else if (itemE.getTagName().equals(EDGE_COLUMN)) {
                    column=edgesTable.getColumn(id);
                    if(column!=null){
                        edgeColumns.addAvailableColumn(column);
                    }
                }
            }
        }
    }
}
