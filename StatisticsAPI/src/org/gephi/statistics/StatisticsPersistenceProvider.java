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
package org.gephi.statistics;

import org.gephi.statistics.StatisticsModelImpl;
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
public class StatisticsPersistenceProvider implements WorkspacePersistenceProvider {

    public Element writeXML(Document document, Workspace workspace) {
        StatisticsModelImpl statModel = workspace.getLookup().lookup(StatisticsModelImpl.class);
        if (statModel != null) {
            Element statModelE = statModel.writeXML(document);
            return statModelE;
        }
        return null;
    }

    public void readXML(Element element, Workspace workspace) {
        StatisticsModelImpl statModel = new StatisticsModelImpl();
        statModel.readXML(element);
        workspace.add(statModel);
    }

    public String getIdentifier() {
        return "statisticsmodel";
    }
}
