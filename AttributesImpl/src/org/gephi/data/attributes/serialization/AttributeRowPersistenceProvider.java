/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.data.attributes.serialization;

import org.gephi.data.attributes.AbstractAttributeModel;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 15000)
public class AttributeRowPersistenceProvider implements WorkspacePersistenceProvider {

    public Element writeXML(Document document, Workspace workspace) {
        AttributeModel model = workspace.getLookup().lookup(AttributeModel.class);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        AttributeRowSerializer serializer = new AttributeRowSerializer();
        if (model != null && graphModel != null && model instanceof AbstractAttributeModel) {
            return serializer.writeRows(document, graphModel);
        }
        return null;
    }

    public void readXML(Element element, Workspace workspace) {
        AttributeModel model = workspace.getLookup().lookup(AttributeModel.class);
        GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
        AttributeRowSerializer serializer = new AttributeRowSerializer();
        if (model != null && graphModel != null && model instanceof AbstractAttributeModel) {
            serializer.readRows(element, graphModel, (AbstractAttributeModel) model);
        }
    }

    public String getIdentifier() {
        return "attributerows";
    }
}
