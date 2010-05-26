package org.gephi.data.attributes.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Škurla
 */
public class IntegerListTest {

    @Test
    public void testCreatingListDefaultSeparator() {
        IntegerList list = new IntegerList("11,22;33");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListGivenSeparator() {
        IntegerList list = new IntegerList("11/22/33", "/");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListPrimitiveArray() {
        IntegerList list = new IntegerList(new int[] {11, 22, 33});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListWrapperArray() {
        IntegerList list = new IntegerList(new Integer[] {11, 22, 33});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testEmptyPrimitiveArray() {
        IntegerList list = new IntegerList(new int [0]);
        assertEquals(list.size(), 0);
    }

    @Test
    public void testEmptyWrapperArray() {
        IntegerList list = new IntegerList(new Integer [0]);
        assertEquals(list.size(), 0);
    }
}