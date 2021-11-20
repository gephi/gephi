package org.gephi.project.io.utils;

import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Workspace;

public class MockXMLPersistenceProviderFailWrite extends MockXMLPersistenceProvider {

    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        throw new RuntimeException("Failed to write");
    }
}
