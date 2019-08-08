package org.gephi.utils;

import org.junit.Assert;
import org.junit.Test;

public class HTMLEscapeTest {

	@Test
	public void testStringToHTMLString() {
		Assert.assertEquals("foo",
			HTMLEscape.stringToHTMLString("foo"));
		Assert.assertEquals("foo &nbsp;bar",
			HTMLEscape.stringToHTMLString("foo  bar"));
		Assert.assertEquals("&quot;foo&quot;",
			HTMLEscape.stringToHTMLString("\"foo\""));
		Assert.assertEquals("foo&amp;bar",
			HTMLEscape.stringToHTMLString("foo&bar"));
		Assert.assertEquals("foo&lt;bar",
			HTMLEscape.stringToHTMLString("foo<bar"));
		Assert.assertEquals("foo&gt;bar",
			HTMLEscape.stringToHTMLString("foo>bar"));
		Assert.assertEquals("foo<br>\nbar",
			HTMLEscape.stringToHTMLString("foo\nbar"));
		Assert.assertEquals("foobar",
			HTMLEscape.stringToHTMLString("foo\rbar"));
		Assert.assertEquals("&#233;",
			HTMLEscape.stringToHTMLString("é"));
	}
}
