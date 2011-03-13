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
package org.gephi.layout;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class LayoutModelPersistenceProvider implements WorkspacePersistenceProvider {

    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        LayoutModelImpl model = workspace.getLookup().lookup(LayoutModelImpl.class);
        if (model != null) {
            try {
                model.writeXML(writer);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void readXML(XMLStreamReader reader, Workspace workspace) {
        LayoutModelImpl model = workspace.getLookup().lookup(LayoutModelImpl.class);
        if (model == null) {
            model = new LayoutModelImpl();
            workspace.add(model);
        }
        try {
            model.readXML(reader);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getIdentifier() {
        return "layoutmodel";
    }
}
