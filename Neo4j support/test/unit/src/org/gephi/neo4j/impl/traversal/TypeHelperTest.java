package org.gephi.neo4j.impl.traversal;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin Škurla
 */
public class TypeHelperTest {

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
    public void testParsingWholeNumberArray() throws NotParsableException {
        Long[] resultArray = TypeHelper.parseWholeNumberArray(" [   12 , 1,2 ,5 ] ");

        assertArrayEquals(resultArray, new Long[] {12L, 1L, 2L, 5L});
    }
}