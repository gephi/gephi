/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
