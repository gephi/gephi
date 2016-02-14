/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.desktop.datalab.persistence;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.desktop.datalab.AvailableColumnsModel;
import org.gephi.desktop.datalab.DataTablesModel;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 16000)
public class DataLaboratoryPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    private static final String AVAILABLE_COLUMNS = "availablecolumns";
    private static final String NODE_COLUMN = "nodecolumn";
    private static final String EDGE_COLUMN = "edgecolumn";

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        DataTablesModel dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
        if (dataTablesModel == null) {
            workspace.add(dataTablesModel = new DataTablesModel(workspace));
        }
        try {
            writeDataTablesModel(writer, dataTablesModel);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        try {
            readDataTablesModel(reader, workspace);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return AVAILABLE_COLUMNS;
    }

    private void writeDataTablesModel(XMLStreamWriter writer, DataTablesModel dataTablesModel) throws XMLStreamException {
        for (Column column : dataTablesModel.getNodeAvailableColumnsModel().getAvailableColumns()) {
            writer.writeStartElement(NODE_COLUMN);
            writer.writeAttribute("id", String.valueOf(column.getIndex()));
            writer.writeEndElement();
        }

        for (Column column : dataTablesModel.getEdgeAvailableColumnsModel().getAvailableColumns()) {
            writer.writeStartElement(EDGE_COLUMN);
            writer.writeAttribute("id", String.valueOf(column.getIndex()));
            writer.writeEndElement();
        }
    }

    private void readDataTablesModel(XMLStreamReader reader, Workspace workspace) throws XMLStreamException {
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        Table nodesTable = graphModel.getNodeTable();
        Table edgesTable = graphModel.getEdgeTable();
        DataTablesModel dataTablesModel = workspace.getLookup().lookup(DataTablesModel.class);
        if (dataTablesModel == null) {
            workspace.add(dataTablesModel = new DataTablesModel(workspace));
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
                    Column column = nodesTable.getColumn(id);
                    if (column != null) {
                        nodeColumns.addAvailableColumn(column);
                    }
                } else if (EDGE_COLUMN.equalsIgnoreCase(name)) {
                    Integer id = Integer.parseInt(reader.getAttributeValue(null, "id"));
                    Column column = edgesTable.getColumn(id);
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
