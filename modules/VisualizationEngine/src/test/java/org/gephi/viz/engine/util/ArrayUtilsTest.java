package org.gephi.viz.engine.util;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Eduardo Ramos
 */
public class ArrayUtilsTest {

    public ArrayUtilsTest() {
    }

    @Test
    public void testRepeat() {
        int elems = 4;
        int times = 25;
        float[] arr = new float[elems * times];

        arr[0] = 1;
        arr[1] = 2;
        arr[2] = 2.5f;
        arr[3] = 4;

        float expected[] = new float[arr.length];
        for (int i = 0; i < times; i++) {
            System.arraycopy(arr, 0, expected, i * elems, elems);
        }

        int nextIndex = ArrayUtils.repeat(arr, 0, elems, times);

        Assert.assertArrayEquals(expected, arr, 0.01f);
        Assert.assertEquals(arr.length, nextIndex);
    }

    @Test
    public void testRepeatOffset() {
        int elems = 4;
        int offset = elems;
        int times = 25;
        float[] arr = new float[elems * times];

        arr[0] = 0;
        arr[1] = 0;
        arr[2] = 0;
        arr[3] = 0;
        arr[4] = 1;
        arr[5] = 2;
        arr[6] = -3;
        arr[7] = 5;

        float expected[] = new float[arr.length];
        for (int i = 0; i < times - 1; i++) {
            System.arraycopy(arr, offset, expected, offset + i * elems, elems);
        }

        int nextIndex = ArrayUtils.repeat(arr, offset, elems, times - 1);
        
        Assert.assertArrayEquals(expected, arr, 0.01f);
        Assert.assertEquals(arr.length, nextIndex);
    }

}
