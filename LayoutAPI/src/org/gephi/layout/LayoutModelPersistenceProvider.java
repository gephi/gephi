/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.layout;

import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class LayoutModelPersistenceProvider implements WorkspacePersistenceProvider {

    public Element writeXML(Document document, Workspace workspace) {
        LayoutModelImpl model = workspace.getLookup().lookup(LayoutModelImpl.class);
        if(model!=null) {
            return model.writeXML(document);
        }
        return null;
    }

    public void readXML(Element element, Workspace workspace) {
        LayoutModelImpl model = new LayoutModelImpl();
        model.readXML(element);
        workspace.add(model);
    }

    public String getIdentifier() {
        return "layoutmodel";
    }
}
