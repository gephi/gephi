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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.model.TemporaryAttributeModel;
import org.gephi.data.attributes.type.StringList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Mathieu
 */
public class AttributeClassTest {

    private AbstractAttributeModel manager;
    private AttributeTableImpl nodeClass;
    private AttributeFactoryImpl factory;
    //Test map
    private Map<String, AttributeColumnImpl> columnMap;
    private List<AttributeRowImpl> rows;

    public AttributeClassTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        manager = new TemporaryAttributeModel();//Id and Label columns are automatically created at indexes 0 and 1
        nodeClass = manager.getNodeTable();
        factory = new AttributeFactoryImpl(manager);
        columnMap = new HashMap<String, AttributeColumnImpl>();
        rows = new ArrayList<AttributeRowImpl>();
        
        AttributeColumnImpl co1 = nodeClass.addColumn("col1", "Column 1", AttributeType.STRING, AttributeOrigin.DATA, "nil");
        AttributeColumnImpl co2 = nodeClass.addColumn("col2", "Column 2", AttributeType.INT, AttributeOrigin.PROPERTY, 0);
        AttributeColumnImpl co3 = nodeClass.addColumn("col3", "Column 3", AttributeType.LIST_STRING, AttributeOrigin.DATA, new StringList("nothing", ","));
        AttributeColumnImpl co4 = nodeClass.addColumn("col4", "Column 4", AttributeType.STRING, AttributeOrigin.COMPUTED, "zero");
        AttributeColumnImpl co5 = nodeClass.addColumn("col5", "Column 5", AttributeType.BOOLEAN, AttributeOrigin.DATA, true);
        AttributeColumnImpl co6 = nodeClass.addColumn("col6", "Column 6", AttributeType.STRING, AttributeOrigin.DATA, "default");

        columnMap.put("col1", co1);
        columnMap.put("col2", co2);
        columnMap.put("col3", co3);
        columnMap.put("col4", co4);
        columnMap.put("col5", co5);
        columnMap.put("col6", co6);

        //Rows
        for (int i = 0; i < 10; i++) {
            AttributeRowImpl r = factory.newNodeRow(null);
            r.setValue(co1, "col1value " + i);
            r.setValue("col2", i);
            r.setValue("Column 3", null);
            r.setValue(5, "col4value " + i);
            r.setValue(factory.newValue(co5, false));
            rows.add(r);
        }

    }

    @After
    public void tearDown() {
        manager = null;
    }

    @Test
    public void testGetColumn() {
        assertSame(columnMap.get("col1"), nodeClass.getColumn("col1"));
        assertSame(columnMap.get("col1"), nodeClass.getColumn("Column 1"));
        assertSame(columnMap.get("col1"), nodeClass.getColumn(2));
        assertTrue(nodeClass.hasColumn("col1"));
        assertTrue(nodeClass.hasColumn("Column 1"));
    }

    @Test
    public void testValues() {
        int i = 0;

        AttributeRow row = rows.get(0);
        assertEquals("col1value 0", row.getValue(columnMap.get("col1")));
        assertEquals(0, row.getValue(columnMap.get("col2")));
        assertNull(row.getValue(columnMap.get("col3")));
        assertEquals("col4value 0", row.getValue(columnMap.get("col4")));
        assertEquals(false, row.getValue(columnMap.get("col5")));
        assertEquals(columnMap.get("col6").getDefaultValue(), row.getValue(columnMap.get("col6")));

        showValues(row);
    }

    @Test
    public void testAddColumn() {
        AttributeColumnImpl co7 = nodeClass.addColumn("col7", "Column 7", AttributeType.STRING, AttributeOrigin.DATA, "def");
        columnMap.put("col7", co7);

        //Test GetColumn
        assertSame(co7, nodeClass.getColumn("col7"));

        //Test value
        AttributeRow row = rows.get(0);
        assertEquals(columnMap.get("col7").getDefaultValue(), row.getValue(columnMap.get("col7")));
        row.setValue(co7, "test");
        assertEquals("test", row.getValue(columnMap.get("col7")));

        showValues(row);
    }

    @Test
    public void testDeleteColumn() {
        nodeClass.removeColumn(columnMap.get("col4"));

        //Test GetColumn
        assertNull(nodeClass.getColumn("col4"));

        //Test value
        AttributeRow row = rows.get(0);
        assertNull(row.getValue(columnMap.get("col4")));

        showValues(row);
    }

    public void showValues(AttributeRow row) {
        System.out.print("Values: ");
        for (AttributeValue val : row.getValues()) {
            System.out.print("#" + val.getValue() + " ");
        }
        System.out.println();
    }
}