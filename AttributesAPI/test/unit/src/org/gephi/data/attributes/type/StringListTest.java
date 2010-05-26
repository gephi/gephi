package org.gephi.data.attributes.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Škurla
 */
public class StringListTest {

    @Test
    public void testDefaultSeparator() {
        StringList list = new StringList("aa,bb;cc");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testGivenSeparator() {
        StringList list = new StringList("aa/bb/cc", "/");
        assertEquals(list.size(), 3);
    }

    @Test
    public void testStringArray() {
        StringList list = new StringList(new String[] {"aa", "bb", "cc"});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testCharArray() {
        StringList list = new StringList(new char[] {'a', 'b', 'c'});
        assertEquals(list.size(), 3);
    }

    @Test
    public void testEmptyArray() {
        StringList list = new StringList(new String[0]);
        assertEquals(list.size(), 0);
    }
}