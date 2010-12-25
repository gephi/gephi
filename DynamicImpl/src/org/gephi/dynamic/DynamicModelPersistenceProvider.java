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
package org.gephi.dynamic;

import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class DynamicModelPersistenceProvider implements WorkspacePersistenceProvider {

    @Override
    public Element writeXML(Document document, Workspace workspace) {
        DynamicModelImpl model = (DynamicModelImpl) workspace.getLookup().lookup(DynamicModel.class);
        if (model != null) {
            return writeModel(document, model);
        }
        return null;
    }

    @Override
    public void readXML(Element element, Workspace workspace) {
        DynamicControllerImpl dynamicController = (DynamicControllerImpl) Lookup.getDefault().lookup(DynamicController.class);
        DynamicModelImpl dynamicModelImpl = new DynamicModelImpl(dynamicController, workspace);
        readModel(element, dynamicModelImpl);
        workspace.add(dynamicModelImpl);
    }

    @Override
    public String getIdentifier() {
        return "dynamicmodel";
    }

    public Element writeModel(Document document, DynamicModelImpl model) {
        Element modelE = document.createElement("dynamicmodel");

        Element timeFormatE = document.createElement("timeformat");
        if (model.getTimeFormat().equals(DynamicModel.TimeFormat.DATE)) {
            timeFormatE.setAttribute("value", "date");
        } else {
            timeFormatE.setAttribute("value", "double");
        }
        modelE.appendChild(timeFormatE);
        return modelE;
    }

    public void readModel(Element modelE, DynamicModelImpl model) {
        NodeList modelListE = modelE.getChildNodes();
        for (int i = 0; i < modelListE.getLength(); i++) {
            if (modelListE.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element itemE = (Element) modelListE.item(i);
                if (itemE.getTagName().equals("timeformat")) {
                    String val = itemE.getAttribute("value");
                    if (val.equals("date")) {
                        model.setTimeFormat(DynamicModel.TimeFormat.DATE);
                    } else {
                        model.setTimeFormat(DynamicModel.TimeFormat.DOUBLE);
                    }
                }
            }
        }
    }
}
