package org.gephi.data.attributes.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Škurla
 */
public class StringListTest {

    @Test
    public void testCreatingListFromStringWithDefaultSeparator() {
        StringList list = new StringList("aa,bb;cc");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromStringWithGivenSeparator() {
        StringList list = new StringList("aa/bb/cc", "/");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromStringArray() {
        StringList list = new StringList(new String[] {"aa", "bb", "cc"});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromCharArray() {
        StringList list = new StringList(new char[] {'a', 'b', 'c'});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCreatingListFromEmptyStringArray() {
        StringList list = new StringList(new String[0]);
        assertEquals(list.size(), 0);
    }

    @Test
    public void testCreatingListFromEmptyCharArray() {
        StringList list = new StringList(new char[0]);
        assertEquals(list.size(), 0);
    }
}