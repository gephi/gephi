package org.gephi.data.attributes.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Škurla
 */
public class IntegerListTest {

    @Test
    public void testCreatingListFromStringWithDefaultSeparator() {
        IntegerList list = new IntegerList("11,22;33");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromStringWithGivenSeparator() {
        IntegerList list = new IntegerList("11/22/33", "/");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromPrimitiveArray() {
        IntegerList list = new IntegerList(new int[] {11, 22, 33});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromWrapperArray() {
        IntegerList list = new IntegerList(new Integer[] {11, 22, 33});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromEmptyPrimitiveArray() {
        IntegerList list = new IntegerList(new int [0]);
        assertEquals(list.size(), 0);
    }

    @Test
    public void testCreatingListFromEmptyWrapperArray() {
        IntegerList list = new IntegerList(new Integer [0]);
        assertEquals(list.size(), 0);
    }
}