package org.gephi.neo4j.impl.traversal;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin Škurla
 */
public class TypeHelperTest {

    @Test
    public void testParsingWholeNumberArray() throws NotParsableException {
        Long[] resultArray = TypeHelper.parseWholeNumberArray(" [   12 , 1,2 ,5 ] ");

        assertArrayEquals(resultArray, new Long[] {12L, 1L, 2L, 5L});
    }

    @Test(expected=NotParsableException.class)
    public void testParsingWholeNumberArrayWrongInput1() throws NotParsableException {
        TypeHelper.parseWholeNumberArray("[]");
    }

    @Test(expected=NotParsableException.class)
    public void testParsingWholeNumberArrayWrongInput2() throws NotParsableException {
        TypeHelper.parseWholeNumberArray("[,]");
    }

    @Test(expected=NotParsableException.class)
    public void testParsingWholeNumberArrayWrongInput3() throws NotParsableException {
        TypeHelper.parseWholeNumberArray("[12,]");
    }

    @Test
    public void testParsingRealNumberArray() throws NotParsableException {
        Double[] resultArray = TypeHelper.parseRealNumberArray("[12 ,1.8 , 2.5]");

        assertArrayEquals(resultArray, new Double[] {12D, 1.8, 2.5});
    }

    @Test(expected=NotParsableException.class)
    public void testParsingRealNumberArrayWrongInput() throws NotParsableException {
        TypeHelper.parseRealNumberArray("[12d ,1.8f , 2.5e10]");
    }

    @Test
    public void testParsingBooleanArray() throws NotParsableException {
        Boolean[] resultArray = TypeHelper.parseBooleanArray("[true , false,true]");

        assertArrayEquals(resultArray, new Boolean[] {true, false, true});
    }

    @Test
    public void testParsingCharacterArray() throws NotParsableException {
        Character[] resultArray = TypeHelper.parseCharacterArray("[  a ,b, c, d]");

        assertArrayEquals(resultArray, new Character[] {'a', 'b', 'c', 'd'});
    }

    @Test(expected=NotParsableException.class)
    public void testParsingCharacterArrayWrongInput() throws NotParsableException {
        TypeHelper.parseCharacterArray("[  a ,b, c, de]");
    }

    @Test
    public void testParsingStringArray() throws NotParsableException {
        String[] resultArray = TypeHelper.parseStringArray("[_a, bb,ccc , dddd]");

        assertArrayEquals(resultArray, new String[] {"_a", "bb", "ccc", "dddd"});
    }

    @Test(expected=NotParsableException.class)
    public void testParsingStringArrayWrongInput() throws NotParsableException {
        TypeHelper.parseStringArray("[as+]");
    }
}