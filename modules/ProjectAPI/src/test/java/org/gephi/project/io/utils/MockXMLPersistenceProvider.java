package org.gephi.project.io.utils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;

public class MockXMLPersistenceProvider implements WorkspaceXMLPersistenceProvider {

    public static final String TXT = "txt";
    private String readText;

    public MockXMLPersistenceProvider() {
    }

    @Override
    public String getIdentifier() {
        return "mock";
    }

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        try {
            writer.writeCharacters(TXT);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        try {
            while (reader.hasNext()) {
                Integer eventType = reader.next();
                if (eventType.equals(XMLEvent.CHARACTERS)) {
                    readText = reader.getText();
                }
            }
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getReadText() {
        return readText;
    }
}
