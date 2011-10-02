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
package org.gephi.io.importer.plugin.file;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.StringList;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.gephi.io.importer.api.Report;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class ImporterGEXFTest {

    private ImporterGEXF importer;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        importer = new ImporterGEXF();
        try {
            URL url = getClass().getResource("/org/gephi/io/importer/plugin/file/testparser.gexf");
            File file = new File(url.toURI());
            importer.setReader(new FileReader(file));
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @After
    public void tearDown() {
        importer = null;
    }

    @Test
    public void testCalendar() {
        String date = "2000-01-01";
		double d = DynamicUtilities.getDoubleFromXMLDateString(date);
		String date2 = DynamicUtilities.getXMLDateStringFromDouble(d);
		assertEquals(date, date2);
    }

    @Test
    public void testAttributeModel() {
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        container.setReport(new Report());
        importer.execute(container.getLoader());

        ContainerUnloader unloader = container.getUnloader();
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("0", AttributeType.DYNAMIC_STRING));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("1", AttributeType.DYNAMIC_FLOAT));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("2", AttributeType.LIST_STRING));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("3", AttributeType.DYNAMIC_FLOAT));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("4", AttributeType.DYNAMIC_FLOAT));

        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("type", AttributeType.DYNAMIC_STRING));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("Attribute 1", AttributeType.DYNAMIC_FLOAT));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("Attribute 2", AttributeType.LIST_STRING));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("Attribute 3", AttributeType.DYNAMIC_FLOAT));
        assertNotNull(unloader.getAttributeModel().getNodeTable().getColumn("Attribute 4", AttributeType.DYNAMIC_FLOAT));

        assertNotNull(unloader.getAttributeModel().getEdgeTable().getColumn("weight", AttributeType.DYNAMIC_FLOAT));
    }

    @Test
    public void testNode() {
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        container.setReport(new Report());
        importer.execute(container.getLoader());

        NodeDraftGetter n1 = (NodeDraftGetter) container.getLoader().getNode("4c175b4b2c9016b90d1b4bc2");
        NodeDraftGetter n2 = (NodeDraftGetter) container.getLoader().getNode("4c175b542c9016b9ce1b4bc2");
        NodeDraftGetter n3 = (NodeDraftGetter) container.getLoader().getNode("4c175b4b2c9016b9071b4bc2");
        NodeDraftGetter n4 = (NodeDraftGetter) container.getLoader().getNode("4c175b4b2c9016b9101b4bc2");
        NodeDraftGetter n5 = (NodeDraftGetter) container.getLoader().getNode("4c175b4b2c9016b93a1b4bc2");
        NodeDraftGetter n6 = (NodeDraftGetter) container.getLoader().getNode("4c175b4a2c9016b9a51a4bc2");
        NodeDraftGetter n7 = (NodeDraftGetter) container.getLoader().getNode("4c175b4b2c9016b9ba1a4bc2");
        NodeDraftGetter n8 = (NodeDraftGetter) container.getLoader().getNode("4c175b4b2c9016b9ed1a4bc2");
        NodeDraftGetter n9 = (NodeDraftGetter) container.getLoader().getNode("4c175b4b2c9016b9f61a4bc2");

        assertNotNull(n1);
        assertNotNull(n2);
        assertNotNull(n3);
        assertNotNull(n4);
        assertNotNull(n5);
        assertNotNull(n6);
        assertNotNull(n7);
        assertNotNull(n8);
        assertNotNull(n9);

        ContainerUnloader unloader = container.getUnloader();
        AttributeColumn col0 = unloader.getAttributeModel().getNodeTable().getColumn("0");
        AttributeColumn col1 = unloader.getAttributeModel().getNodeTable().getColumn("1");
        AttributeColumn col2 = unloader.getAttributeModel().getNodeTable().getColumn("2");
        AttributeColumn col3 = unloader.getAttributeModel().getNodeTable().getColumn("3");
        AttributeColumn col4 = unloader.getAttributeModel().getNodeTable().getColumn("4");

        try {
            assertEquals("Node 1", n1.getLabel());
            assertEquals("2000-01-01", DynamicUtilities.getXMLDateStringFromDouble(n1.getTimeInterval().getValues().get(0)[0]));
            assertEquals("2000-12-31", DynamicUtilities.getXMLDateStringFromDouble(n1.getTimeInterval().getValues().get(0)[1]));

            AttributeValue[] values1 = n1.getAttributeRow().getValues();
            assertEquals("0", values1[col0.getIndex()].getColumn().getId());
            assertEquals("3", values1[col3.getIndex()].getColumn().getId());
            assertEquals("Author", ((DynamicType) values1[col0.getIndex()].getValue()).getValue());
            assertEquals(new Float(1), ((DynamicType) values1[col3.getIndex()].getValue()).getValue());

            AttributeValue[] values2 = n2.getAttributeRow().getValues();
            assertEquals("0", values2[col0.getIndex()].getColumn().getId());
            assertEquals("2", values2[col2.getIndex()].getColumn().getId());
            assertEquals("Author", ((DynamicType) values2[col0.getIndex()].getValue()).getValue());
            assertEquals(new StringList("String1, String2, String 3"), values2[col2.getIndex()].getValue());

            AttributeValue[] values3 = n3.getAttributeRow().getValues();
            DynamicType val4 = (DynamicType) values3[col4.getIndex()].getValue();
            double low = DynamicUtilities.getDoubleFromXMLDateString("2009-01-01");
            double high = DynamicUtilities.getDoubleFromXMLDateString("2009-12-31");
            assertEquals(new Float(3f), val4.getValue(low, high));

            assertEquals("2000-01-01", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(0)[0]));
            assertEquals("2000-01-15", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(0)[1]));
            assertEquals("2001-01-30", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(1)[0]));
            assertEquals("2001-02-01", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(1)[1]));
            assertEquals(2, n3.getTimeInterval().getValues().size());

            container.verify();

            assertEquals("2000-01-01", DynamicUtilities.getXMLDateStringFromDouble(n4.getTimeInterval().getValues().get(0)[0]));
            assertEquals("2010-12-31", DynamicUtilities.getXMLDateStringFromDouble(n4.getTimeInterval().getValues().get(0)[1]));

            assertEquals("2000-01-01", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(0)[0]));
            assertEquals("2000-01-15", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(0)[1]));
            assertEquals("2001-01-30", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(1)[0]));
            assertEquals("2001-02-01", DynamicUtilities.getXMLDateStringFromDouble(n3.getTimeInterval().getValues().get(1)[1]));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        container.closeLoader();
        System.out.println(container.getReport().getText());
    }

    @Test
    public void testHierarchy() {
        final Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
        container.setReport(new Report());
        importer.execute(container.getLoader());

        NodeDraftGetter na = (NodeDraftGetter) container.getLoader().getNode("a");
        NodeDraftGetter nb = (NodeDraftGetter) container.getLoader().getNode("b");
        NodeDraftGetter nc = (NodeDraftGetter) container.getLoader().getNode("c");
        NodeDraftGetter nd = (NodeDraftGetter) container.getLoader().getNode("d");
        NodeDraftGetter ne = (NodeDraftGetter) container.getLoader().getNode("e");
        NodeDraftGetter nf = (NodeDraftGetter) container.getLoader().getNode("f");
        NodeDraftGetter ng = (NodeDraftGetter) container.getLoader().getNode("g");
        NodeDraftGetter nh = (NodeDraftGetter) container.getLoader().getNode("h");
        NodeDraftGetter ni = (NodeDraftGetter) container.getLoader().getNode("i");
        NodeDraftGetter nj = (NodeDraftGetter) container.getLoader().getNode("j");

        assertNotNull(na);
        assertNotNull(nb);
        assertNotNull(nc);
        assertNotNull(nd);
        assertNotNull(ne);
        assertNotNull(nf);
        assertNotNull(ng);

        assertNull(na.getParents());
        assertEquals(1, nb.getParents().length);
        assertEquals(1, nc.getParents().length);
        assertEquals(1, nd.getParents().length);
        assertEquals(1, ne.getParents().length);
        assertEquals(1, nf.getParents().length);
        assertEquals(1, ng.getParents().length);

        assertEquals(na, nb.getParents()[0]);
        assertEquals(nb, nc.getParents()[0]);
        assertEquals(nb, nd.getParents()[0]);
        assertEquals(na, ne.getParents()[0]);
        assertEquals(ne, nf.getParents()[0]);
        assertEquals(ne, ng.getParents()[0]);

        assertEquals(nf, nh.getParents()[0]);
        assertEquals(nj, ni.getParents()[0]);

        System.out.println(container.getReport().getText());
    }
}
