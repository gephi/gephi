package org.gephi.visualization.component;

import java.awt.Color;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import org.junit.Assert;
import org.junit.Test;

public class FocusRingPanelTest {

    @Test
    public void testRingHasSameInsetsAsEmptyBorder() {
        FocusRingPanel panel = new FocusRingPanel(new JLabel());
        Insets unfocused = panel.getInsets();
        Assert.assertEquals(FocusRingPanel.THICKNESS, unfocused.left);
        Assert.assertFalse(panel.getBorder() instanceof LineBorder);

        panel.setFocused(true);
        Assert.assertTrue(panel.isFocused());
        Assert.assertTrue(panel.getBorder() instanceof LineBorder);
        Assert.assertEquals(unfocused, panel.getInsets());

        panel.setFocused(false);
        Assert.assertFalse(panel.isFocused());
        Assert.assertFalse(panel.getBorder() instanceof LineBorder);
        Assert.assertEquals(unfocused, panel.getInsets());
    }

    @Test
    public void testRingFollowsThemeColor() {
        FocusRingPanel panel = new FocusRingPanel(new JLabel());
        panel.setFocused(true);

        UIManager.put("Component.accentColor", new Color(0, 0, 255));
        try {
            panel.updateUI();
            Assert.assertTrue(panel.getBorder() instanceof LineBorder);
            Color ring = ((LineBorder) panel.getBorder()).getLineColor();
            Assert.assertEquals(new Color(0, 0, 255), ring);
        } finally {
            UIManager.put("Component.accentColor", null);
        }
    }

    @Test
    public void testFocusColorOnMidGreyBackground() {
        UIManager.put("Component.accentColor", new Color(0x26, 0x75, 0xBF));
        try {
            for (int grey = 0x80; grey <= 0xD0; grey += 0x08) {
                Color background = new Color(grey, grey, grey);
                Assert.assertTrue("background " + Integer.toHexString(grey),
                    contrast(FocusRingPanel.focusColor(background), background) >= 3.0);
            }
        } finally {
            UIManager.put("Component.accentColor", null);
        }
    }

    @Test
    public void testCanvasFocusedNeedsAwtFocusWhenEventsPassThrough() {
        Assert.assertTrue(VizEngineGraphCanvasManager.isCanvasFocused(true, false, false));
        Assert.assertTrue(VizEngineGraphCanvasManager.isCanvasFocused(true, true, true));
        Assert.assertFalse(VizEngineGraphCanvasManager.isCanvasFocused(true, true, false));
        Assert.assertFalse(VizEngineGraphCanvasManager.isCanvasFocused(false, false, true));
        Assert.assertFalse(VizEngineGraphCanvasManager.isCanvasFocused(false, true, true));
    }

    @Test
    public void testFocusColorStandsOutFromBackground() {
        UIManager.put("Component.accentColor", new Color(75, 110, 175));
        try {
            Color dark = FocusRingPanel.focusColor(new Color(60, 63, 65));
            Color light = FocusRingPanel.focusColor(new Color(242, 242, 242));
            Assert.assertTrue(contrast(dark, new Color(60, 63, 65)) >= 3.0);
            Assert.assertTrue(contrast(light, new Color(242, 242, 242)) >= 3.0);
        } finally {
            UIManager.put("Component.accentColor", null);
        }
    }

    private static double contrast(Color a, Color b) {
        double la = luminance(a);
        double lb = luminance(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    private static double luminance(Color c) {
        return 0.2126 * linear(c.getRed()) + 0.7152 * linear(c.getGreen()) + 0.0722 * linear(c.getBlue());
    }

    private static double linear(int channel) {
        double v = channel / 255.0;
        return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    }
}
