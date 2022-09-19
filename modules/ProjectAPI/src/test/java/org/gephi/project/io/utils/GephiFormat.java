package org.gephi.project.io.utils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.junit.Assert;

public class GephiFormat {

    public static Workspace testXMLPersistenceProvider(WorkspaceXMLPersistenceProvider provider,
                                                  Workspace workspace) throws Exception {
        Assert.assertNotNull(provider.getIdentifier());

        String xmlString = toString(provider, workspace);
        Workspace newWorkspace = fromString(provider, xmlString);
        String xmlStringAgain = toString(provider, newWorkspace);

        Assert.assertEquals(xmlString, xmlStringAgain);
        return newWorkspace;
    }

    private static Workspace fromString(WorkspaceXMLPersistenceProvider provider, String xmlString)
        throws XMLStreamException, IOException {
        Workspace destinationWorkspace = new WorkspaceImpl(null, 0);

        StringReader stringReader = new StringReader(xmlString);
        XMLStreamReader reader = newXMLReader(stringReader);
        provider.readXML(reader, destinationWorkspace);
        reader.close();
        stringReader.close();
        return destinationWorkspace;
    }

    private static String toString(WorkspaceXMLPersistenceProvider provider, Workspace workspace)
        throws XMLStreamException, IOException {
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = newXMLWriter(stringWriter);

        String identifier = provider.getIdentifier();
        writer.writeStartDocument("UTF-8", "1.0");
        writer.writeStartElement(identifier);
        provider.writeXML(writer, workspace);
        writer.writeEndElement();
        writer.writeEndDocument();

        writer.close();
        stringWriter.close();
        return stringWriter.toString();
    }

    public static XMLStreamReader newXMLReader(Reader reader) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
            inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        }
        return inputFactory.createXMLStreamReader(reader);
    }

    public static XMLStreamWriter newXMLWriter(Writer writer) throws XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);
        return outputFactory.createXMLStreamWriter(writer);
    }
}
