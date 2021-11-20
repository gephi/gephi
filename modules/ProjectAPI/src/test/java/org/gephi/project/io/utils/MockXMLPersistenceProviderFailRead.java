package org.gephi.project.io.utils;

import javax.xml.stream.XMLStreamReader;
import org.gephi.project.api.Workspace;

public class MockXMLPersistenceProviderFailRead extends MockXMLPersistenceProvider {

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        throw new RuntimeException("Failed to write");
    }
}
