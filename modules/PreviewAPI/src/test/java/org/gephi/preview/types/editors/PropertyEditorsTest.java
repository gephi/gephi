package org.gephi.preview.types.editors;

import java.awt.Color;
import org.junit.Assert;
import org.junit.Test;

public class PropertyEditorsTest {

    private static final Color CUSTOM_RGB = new Color(1,2,3);
    private static final Color CUSTOM_RGBA = new Color(1,2,3,4);

    @Test
    public void testToTextRgb() {
        MyColorPropertyEditor propertyEditor = new MyColorPropertyEditor();
        String rgb = propertyEditor.toText("foo", CUSTOM_RGB);
        Assert.assertEquals("foo [1,2,3]", rgb);
    }

    @Test
    public void testToTextRgba() {
        MyColorPropertyEditor propertyEditor = new MyColorPropertyEditor();
        String rgba = propertyEditor.toText("foo", CUSTOM_RGBA);
        Assert.assertEquals("foo [1,2,3,4]", rgba);
    }

    @Test
    public void testToColorRgb() {
        MyColorPropertyEditor propertyEditor = new MyColorPropertyEditor();
        Color rgb = propertyEditor.toColor("foo [1,2,3]");
        Assert.assertEquals(CUSTOM_RGB, rgb);
    }

    @Test
    public void testToColorRgba() {
        MyColorPropertyEditor propertyEditor = new MyColorPropertyEditor();
        Color rgba = propertyEditor.toColor("foo [1,2,3,4]");
        Assert.assertEquals(CUSTOM_RGBA, rgba);
    }

    // Utility
    private static class MyColorPropertyEditor extends AbstractColorPropertyEditor { }
}
