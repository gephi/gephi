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
package org.gephi.filters;

import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class FilterModelPersistenceProvider implements WorkspacePersistenceProvider {

    public Element writeXML(Document document, Workspace workspace) {
        FilterModelImpl filterModel = workspace.getLookup().lookup(FilterModelImpl.class);
        if (filterModel != null) {
            Element filterModelE = filterModel.writeXML(document);
            return filterModelE;
        }
        return null;
    }

    public void readXML(Element element, Workspace workspace) {
        FilterModelImpl filterModel = new FilterModelImpl();
        filterModel.readXML(element);
        workspace.add(filterModel);
    }

    public String getIdentifier() {
        return "filtermodel";
    }
}
