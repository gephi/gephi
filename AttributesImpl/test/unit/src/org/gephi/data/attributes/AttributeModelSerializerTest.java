/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
