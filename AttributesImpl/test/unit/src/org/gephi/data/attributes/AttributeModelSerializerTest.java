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

import org.gephi.data.attributes.serialization.AttributeModelSerializer;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.model.IndexedAttributeModel;
import org.gephi.data.attributes.type.StringList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
        AttributeModelSerializer serializer = new AttributeModelSerializer();
        Element e1 = serializer.writeModel(serializer.createDocument(), model);
        String s1 = printXML(e1);
        System.out.println(s1);
        IndexedAttributeModel model2 = new IndexedAttributeModel();
        serializer.readModel(e1, model2);
        Element e2 = serializer.writeModel(serializer.createDocument(), model2);
        String s2 = printXML(e2);
        System.out.println(s2);
        assertEquals(s1, s2);
    }

    private String printXML(org.w3c.dom.Node node) {
        try {
            Source source = new DOMSource(node);
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
}
