/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.gephi.data.attributes;

import java.io.StringReader;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.data.attributes.serialization.AttributeModelSerializer;
import java.io.StringWriter;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.model.IndexedAttributeModel;
import org.gephi.data.attributes.type.StringList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu Bastian
 */
public class AttributeModelSerializerTest {

    private IndexedAttributeModel model;

    @Before
    public void setUp() {
        model = new IndexedAttributeModel();
        AttributeTableImpl nodeTableImpl = model.getNodeTable();
        nodeTableImpl.addColumn("id", "Identifier", AttributeType.INT, AttributeOrigin.DATA, new Integer(0));
        nodeTableImpl.addColumn("url", AttributeType.STRING);
        nodeTableImpl.addColumn("position", "Position&<>\"'$*", AttributeType.FLOAT, AttributeOrigin.PROPERTY, new Float(0));
        nodeTableImpl.addColumn("cats", "Catégories", AttributeType.LIST_STRING, AttributeOrigin.DATA, new StringList("a,b,c,d"));
        AttributeTableImpl edgeTableImpl = model.getEdgeTable();
        edgeTableImpl.addColumn("name", AttributeType.STRING, AttributeOrigin.DATA);
        //edgeTableImpl.addColumn("weight", AttributeType.DOUBLE, AttributeOrigin.DATA);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSerializer() {
        try {
            AttributeModelSerializer serializer = new AttributeModelSerializer();
            StringWriter stringWriter = new StringWriter();
            XMLStreamWriter writer = createWriter(stringWriter);
            serializer.writeModel(writer, model);
            writer.close();
            String s1 = stringWriter.toString();
            System.out.println(s1);
            IndexedAttributeModel model2 = new IndexedAttributeModel();
            StringReader stringReader = new StringReader(s1);
            XMLStreamReader reader = createReader(stringReader);
            serializer.readModel(reader, model2);
            stringWriter = new StringWriter();
            writer = createWriter(stringWriter);
            serializer.writeModel(writer, model2);
            String s2 = stringWriter.toString();
            System.out.println(s2);
            assertEquals(s1, s2);
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private XMLStreamWriter createWriter(StringWriter stringWriter) {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);

        try {
            XMLStreamWriter xmlWriter = outputFactory.createXMLStreamWriter(stringWriter);
            xmlWriter.writeStartDocument("UTF-8", "1.0");
            return xmlWriter;
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private XMLStreamReader createReader(StringReader stringReader) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
            inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        }
        inputFactory.setXMLReporter(new XMLReporter() {

            @Override
            public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                System.out.println("Error:" + errorType + ", message : " + message);
            }
        });
        try {
            return inputFactory.createXMLStreamReader(stringReader);
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
