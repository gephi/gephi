/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.data.attributes.serialization;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 15000)
public class AttributeRowPersistenceProvider implements WorkspacePersistenceProvider {

    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        AttributeModel model = workspace.getLookup().lookup(AttributeModel.class);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        AttributeRowSerializer serializer = new AttributeRowSerializer();
        if (model != null && graphModel != null && model instanceof AbstractAttributeModel) {
            try {
                serializer.writeRows(writer, graphModel);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void readXML(XMLStreamReader reader, Workspace workspace) {
        AttributeModel model = workspace.getLookup().lookup(AttributeModel.class);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        AttributeRowSerializer serializer = new AttributeRowSerializer();
        if (model != null && graphModel != null && model instanceof AbstractAttributeModel) {
            try {
                serializer.readRows(reader, graphModel, (AbstractAttributeModel) model);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public String getIdentifier() {
        return "attributerows";
    }
}
