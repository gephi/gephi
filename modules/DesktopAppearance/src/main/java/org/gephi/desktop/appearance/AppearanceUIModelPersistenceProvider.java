package org.gephi.desktop.appearance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.gephi.utils.Serialization;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class, position = 450)
public class AppearanceUIModelPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        AppearanceUIModel model = workspace.getLookup().lookup(AppearanceUIModel.class);
        if (model != null) {
            try {
                writeXML(writer, model);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        AppearanceUIModel model = workspace.getLookup().lookup(AppearanceUIModel.class);
        AppearanceController appearanceController =
            Lookup.getDefault().lookup(AppearanceController.class);
        if (model == null) {
            model = new AppearanceUIModel(appearanceController.getModel(workspace));
            workspace.add(model);
        }
        try {
            readXML(reader, model);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "appearanceuimodel";
    }

    protected void writeXML(XMLStreamWriter writer, AppearanceUIModel model)
        throws XMLStreamException {
        for (Map.Entry<Function, Map<String, Object>> savedProperty : model.savedProperties.entrySet()) {
            writer.writeStartElement("savedproperty");
            writer.writeAttribute("function", savedProperty.getKey().toString());
            writeSavedProperty(writer, savedProperty.getValue());
            writer.writeEndElement();
        }
    }

    private void writeSavedProperty(XMLStreamWriter writer, Map<String, Object> savedProperty)
        throws XMLStreamException {
        for (Map.Entry<String, Object> entry : savedProperty.entrySet()) {
            writer.writeStartElement("property");
            writer.writeAttribute("key", entry.getKey());
            writer.writeAttribute("value", Serialization.getValueAsText(entry.getValue()));
            writer.writeAttribute("type", entry.getValue().getClass().getName());
            writer.writeEndElement();
        }
    }

    public void readXML(XMLStreamReader reader, AppearanceUIModel model) throws XMLStreamException {
        AppearanceModel appearanceModel = model.appearanceModel;
        Function[] functions = Stream.concat(Arrays.stream(appearanceModel.getNodeFunctions()),
            Arrays.stream(appearanceModel.getEdgeFunctions()))
            .toArray(Function[]::new);

        Function function = null;
        Map<String, Object> properties = null;
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("savedproperty".equalsIgnoreCase(name)) {
                    String functionName = reader.getAttributeValue(null, "function");
                    function = Arrays.stream(functions).filter(f -> f.toString().equals(functionName)).findFirst()
                        .orElse(null);
                    properties = new HashMap<>();
                } else if ("property".equalsIgnoreCase(name) && function != null) {
                    readSavedProperty(reader, properties);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("savedproperty".equalsIgnoreCase(reader.getLocalName()) && function != null) {
                    model.savedProperties.put(function, properties);
                    function = null;
                    properties = null;
                } else if (getIdentifier().equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    private void readSavedProperty(XMLStreamReader reader, Map<String, Object> map) throws XMLStreamException {
        String key = reader.getAttributeValue(null, "key");
        String value = reader.getAttributeValue(null, "value");
        String type = reader.getAttributeValue(null, "type");
        Object val = Serialization.readValueFromText(value, type);
        if (val != null) {
            map.put(key, val);
        }
    }
}

