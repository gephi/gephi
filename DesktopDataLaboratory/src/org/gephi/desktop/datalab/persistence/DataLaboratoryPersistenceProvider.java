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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.desktop.datalab.AvailableColumnsModel;
import org.gephi.desktop.datalab.DataTablesModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 16000)
public class DataLaboratoryPersistenceProvider implements WorkspacePersistenceProvider {

    private static final String AVAILABLE_COLUMNS = "availablecolumns";
    private static final String NODE_COLUMN = "nodecolumn";
    private static final String EDGE_COLUMN = "edgecolumn";

    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        DataTablesModel dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
        if (dataTablesModel == null) {
            workspace.add(dataTablesModel = new DataTablesModel(attributeModel.getNodeTable(), attributeModel.getEdgeTable()));
        }
        try {
            writeDataTablesModel(writer, dataTablesModel);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void readXML(XMLStreamReader reader, Workspace workspace) {
        try {
            readDataTablesModel(reader, workspace);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getIdentifier() {
        return AVAILABLE_COLUMNS;
    }

    private void writeDataTablesModel(XMLStreamWriter writer, DataTablesModel dataTablesModel) throws XMLStreamException {
        writer.writeStartElement(AVAILABLE_COLUMNS);

        for (AttributeColumn column : dataTablesModel.getNodeAvailableColumnsModel().getAvailableColumns()) {
            writer.writeStartElement(NODE_COLUMN);
            writer.writeAttribute("id", String.valueOf(column.getIndex()));
            writer.writeEndElement();
        }

        for (AttributeColumn column : dataTablesModel.getEdgeAvailableColumnsModel().getAvailableColumns()) {
            writer.writeStartElement(EDGE_COLUMN);
            writer.writeAttribute("id", String.valueOf(column.getIndex()));
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private void readDataTablesModel(XMLStreamReader reader, Workspace workspace) throws XMLStreamException {
        AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
        AttributeTable nodesTable = attributeModel.getNodeTable();
        AttributeTable edgesTable = attributeModel.getEdgeTable();
        DataTablesModel dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
        if (dataTablesModel == null) {
            workspace.add(dataTablesModel = new DataTablesModel());
        }
        AvailableColumnsModel nodeColumns = dataTablesModel.getNodeAvailableColumnsModel();
        nodeColumns.removeAllColumns();
        AvailableColumnsModel edgeColumns = dataTablesModel.getEdgeAvailableColumnsModel();
        edgeColumns.removeAllColumns();

        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (NODE_COLUMN.equalsIgnoreCase(name)) {
                    Integer id = Integer.parseInt(reader.getAttributeValue(null, "id"));
                    AttributeColumn column = nodesTable.getColumn(id);
                    if (column != null) {
                        nodeColumns.addAvailableColumn(column);
                    }
                } else if (EDGE_COLUMN.equalsIgnoreCase(name)) {
                    String id = reader.getAttributeValue(null, "id");
                    AttributeColumn column = edgesTable.getColumn(id);
                    if (column != null) {
                        edgeColumns.addAvailableColumn(column);
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (AVAILABLE_COLUMNS.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }
}
