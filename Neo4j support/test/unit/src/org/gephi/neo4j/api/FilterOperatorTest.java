package org.gephi.neo4j.api;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Martin Škurla
 */
public class FilterOperatorTest {

    @Test
    public void testEqualsOperatorOnArraysNotSameArrayLength() {
        byte[] byteArray1 = {1, 2, 3};
        byte[] byteArray2 = {1, 2, 3, 4};

        boolean comparisonResult =
                FilterOperator.EQUALS.executeOnWholeNumberArrays(byteArray1, byteArray2);

        assertFalse(comparisonResult);
    }

    @Test
    public void testEqualsOperatorOnArraysWithNotSameValues() {
        byte[] byteArray1 = {1, 2, 3};
        byte[] byteArray2 = {1, 2, 4};

        boolean comparisonResult =
                FilterOperator.EQUALS.executeOnWholeNumberArrays(byteArray1, byteArray2);

        assertFalse(comparisonResult);
    }

    @Test
    public void testEqualsOperatorOnArraysSameArrays() {
        byte[] byteArray1 = {1, 2, 3};
        byte[] byteArray2 = {1, 2, 3};

        boolean comparisonResult =
                FilterOperator.EQUALS.executeOnWholeNumberArrays(byteArray1, byteArray2);

        assertTrue(comparisonResult);
    }

    @Test
    public void testEqualsOperatorOnPrimitiveAndWrapperArray() {
        byte[] byteArray1 = {1, 2, 3};
        Byte[] byteArray2 = {1, 2, 3};

        boolean comparisonResult =
                FilterOperator.EQUALS.executeOnWholeNumberArrays(byteArray1, byteArray2);

        assertTrue(comparisonResult);
    }

    @Test
    public void testEqualsOperatorDifferentPrimitiveArrays() {
        byte[] byteArray1 = {1, 2, 3};
        long[] byteArray2 = {1, 2, 3};

        boolean comparisonResult =
                FilterOperator.EQUALS.executeOnWholeNumberArrays(byteArray1, byteArray2);

        assertTrue(comparisonResult);
    }
}