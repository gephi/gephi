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
package org.gephi.graph.dhns;

import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.utils.DHNSSerializer;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.api.WorkspacePersistenceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class DhnsPersistenceProvider implements WorkspacePersistenceProvider {

    public Element writeXML(Document document, Workspace workspace) {
        Dhns dhns = workspace.getLookup().lookup(Dhns.class);
        DHNSSerializer serializer = new DHNSSerializer();
        return serializer.writeDhns(document, dhns);
    }

    public void readXML(Element element, Workspace workspace) {
        DhnsGraphController graphController = Lookup.getDefault().lookup(DhnsGraphController.class);
        Dhns dhns = new Dhns(graphController, workspace);
        DHNSSerializer serializer = new DHNSSerializer();
        serializer.readDhns(element, dhns);
    }

    public String getIdentifier() {
        return "dhns";
    }
}
