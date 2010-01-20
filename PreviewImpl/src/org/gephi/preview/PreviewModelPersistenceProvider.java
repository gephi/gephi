/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.preview;

import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.spi.WorkspacePersistenceProvider;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service=WorkspacePersistenceProvider.class)
public class PreviewModelPersistenceProvider implements WorkspacePersistenceProvider {

    public Element writeXML(Document document, Workspace workspace) {
        PreviewModelImpl model = workspace.getLookup().lookup(PreviewModelImpl.class);
        if (model != null) {
            return model.writeXML(document);
        }
        return null;
    }

    public void readXML(Element element, Workspace workspace) {
        PreviewModelImpl model = new PreviewModelImpl();
        model.readXML(element);
        workspace.add(model);
    }

    public String getIdentifier() {
        return "previewmodel";
    }
}
