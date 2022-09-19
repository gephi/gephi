package org.gephi.desktop.preview;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 460)
public class PreviewUIPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        PreviewUIModelImpl model = workspace.getLookup().lookup(PreviewUIModelImpl.class);
        if (model != null) {
            try {
                model.writeXML(writer);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        PreviewUIModelImpl model = workspace.getLookup().lookup(PreviewUIModelImpl.class);
        PreviewModel previewModel = workspace.getLookup().lookup(PreviewModel.class);
        if (previewModel == null) {
            PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
            previewModel = previewController.getModel(workspace);
        }
        if (model == null) {
            model = new PreviewUIModelImpl(previewModel);
            workspace.add(model);
        }
        try {
            model.readXML(reader);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "previewuimodel";
    }
}
