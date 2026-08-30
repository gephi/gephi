package org.gephi.desktop.preview;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.desktop.preview.api.PreviewUIController;
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
        if (model == null) {
            // The model is normally created by WorkspaceImpl.initModels() via the Controller SPI.
            // This branch only triggers if the workspace was constructed without initializing
            // models; create one defensively so legacy/edge-case load paths still work.
            PreviewUIController previewUIController = Lookup.getDefault().lookup(PreviewUIController.class);
            model = new PreviewUIModelImpl(workspace, previewUIController);
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
