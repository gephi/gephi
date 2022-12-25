package org.gephi.desktop.search.plugin;

import org.gephi.desktop.search.plugin.FuzzyElementLabelSearchProvider;
import org.gephi.graph.api.Node;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FuzzyElementLabelSearchProviderTest {

    @Mock
    private Node node;

    @Test
    public void testHtmlFirst() {
        Mockito.when(node.getLabel()).thenReturn("foobar");
        Assert.assertTrue(
            new FuzzyElementLabelSearchProvider().toHtmlDisplay(node, "foo").contains("<b>foo</b>bar"));
    }

    @Test
    public void testHtmlLast() {
        Mockito.when(node.getLabel()).thenReturn("foobar");
        Assert.assertTrue(
            new FuzzyElementLabelSearchProvider().toHtmlDisplay(node, "bar").contains("foo<b>bar</b>"));
    }

    @Test
    public void testHtmlMiddle() {
        Mockito.when(node.getLabel()).thenReturn("foobar");
        Assert.assertTrue(
            new FuzzyElementLabelSearchProvider().toHtmlDisplay(node, "oo").contains("f<b>oo</b>bar"));
    }

    @Test
    public void testFirstOnly() {
        Mockito.when(node.getLabel()).thenReturn("foobarfoo");
        Assert.assertTrue(
            new FuzzyElementLabelSearchProvider().toHtmlDisplay(node, "oo").contains("f<b>oo</b>barfoo"));
    }

    @Test
    public void testLowerCase() {
        Mockito.when(node.getLabel()).thenReturn("FooBar");
        Assert.assertTrue(
            new FuzzyElementLabelSearchProvider().toHtmlDisplay(node, "Foo").contains("<b>Foo</b>Bar"));
    }
}
