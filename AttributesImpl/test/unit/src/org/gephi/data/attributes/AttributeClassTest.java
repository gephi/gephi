/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import org.gephi.data.attributes.manager.TemporaryAttributeManager;
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

    private AbstractAttributeManager manager;
    private AbstractAttributeClass nodeClass;
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
        manager = new TemporaryAttributeManager();
        nodeClass = manager.getNodeClass();
        factory = new AttributeFactoryImpl(manager);
        columnMap = new HashMap<String, AttributeColumnImpl>();
        rows = new ArrayList<AttributeRowImpl>();

        AttributeColumnImpl co1 = nodeClass.addAttributeColumn("col1", "Column 1", AttributeType.STRING, AttributeOrigin.DATA, "nil");
        AttributeColumnImpl co2 = nodeClass.addAttributeColumn("col2", "Column 2", AttributeType.INT, AttributeOrigin.PROPERTY, 0);
        AttributeColumnImpl co3 = nodeClass.addAttributeColumn("col3", "Column 3", AttributeType.LIST_STRING, AttributeOrigin.DATA, new StringList("nothing", ","));
        AttributeColumnImpl co4 = nodeClass.addAttributeColumn("col4", "Column 4", AttributeType.STRING, AttributeOrigin.COMPUTED, "zero");
        AttributeColumnImpl co5 = nodeClass.addAttributeColumn("col5", "Column 5", AttributeType.BOOLEAN, AttributeOrigin.DATA, true);
        AttributeColumnImpl co6 = nodeClass.addAttributeColumn("col6", "Column 6", AttributeType.STRING, AttributeOrigin.DATA, "default");

        columnMap.put("col1", co1);
        columnMap.put("col2", co2);
        columnMap.put("col3", co3);
        columnMap.put("col4", co4);
        columnMap.put("col5", co5);
        columnMap.put("col6", co6);

        //Rows
        for (int i = 0; i < 10; i++) {
            AttributeRowImpl r = factory.newNodeRow();
            r.setValue(co1, "col1value " + i);
            r.setValue("col2", i);
            r.setValue("Column 3", null);
            r.setValue(3, "col4value " + i);
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
        assertSame(columnMap.get("col1"), nodeClass.getAttributeColumn("col1"));
        assertSame(columnMap.get("col1"), nodeClass.getAttributeColumn("Column 1"));
        assertSame(columnMap.get("col1"), nodeClass.getAttributeColumn(0));
        assertTrue(nodeClass.hasAttributeColumn("col1"));
        assertTrue(nodeClass.hasAttributeColumn("Column 1"));
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
        AttributeColumnImpl co7 = nodeClass.addAttributeColumn("col7", "Column 7", AttributeType.STRING, AttributeOrigin.DATA, "def");
        columnMap.put("col7", co7);

        //Test GetColumn
        assertSame(co7, nodeClass.getAttributeColumn("col7"));

        //Test value
        AttributeRow row = rows.get(0);
        assertEquals(columnMap.get("col7").getDefaultValue(), row.getValue(columnMap.get("col7")));
        row.setValue(co7, "test");
        assertEquals("test", row.getValue(columnMap.get("col7")));

        showValues(row);
    }

    @Test
    public void testDeleteColumn() {
        nodeClass.removeAttributeColumn(columnMap.get("col4"));

        //Test GetColumn
        assertNull(nodeClass.getAttributeColumn("col4"));

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