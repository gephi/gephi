package org.gephi.ui.utils;

import org.junit.Assert;
import org.junit.Test;

public class PrefsUtilsTest {

	@Test
	public void testFloatArrayToString() {
		Assert.assertEquals("1.0, 2.0, 3.0",
			PrefsUtils.floatArrayToString(new float[] { 1.0f, 2.0f, 3.0f }));
	}

	@Test
	public void testStringToFloatArray() {
		Assert.assertArrayEquals(new float[] { 1.0f, 2.0f, 3.0f },
			PrefsUtils.stringToFloatArray("1, 2, 3"), 0.0f);
	}
}
