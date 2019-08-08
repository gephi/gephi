package org.gephi.project.io;

import org.junit.Assert;
import org.junit.Test;

public class XMLCharTest {

	@Test
	public void testIsValid() {
		Assert.assertFalse(XMLChar.isValid(1_114_113));

		Assert.assertTrue(XMLChar.isValid(1_048_577));
	}
}
