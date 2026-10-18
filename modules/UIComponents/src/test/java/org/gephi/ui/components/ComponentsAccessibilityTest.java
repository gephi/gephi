package org.gephi.ui.components;

import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.gephi.ui.components.gradientslider.GradientSlider;
import org.junit.Assert;
import org.junit.Test;

public class ComponentsAccessibilityTest {

    @Test
    public void testRangeSliderPanelNamesItsControls() {
        JRangeSliderPanel panel = new JRangeSliderPanel();

        Assert.assertEquals("Range slider", panel.getSlider().getAccessibleContext().getAccessibleName());
        JTextField minimum = null;
        JTextField maximum = null;
        for (java.awt.Component c : panel.getComponents()) {
            if (c instanceof JTextField) {
                String name = ((JTextField) c).getAccessibleContext().getAccessibleName();
                if ("Range minimum".equals(name)) {
                    minimum = (JTextField) c;
                } else if ("Range maximum".equals(name)) {
                    maximum = (JTextField) c;
                }
            }
        }
        Assert.assertNotNull(minimum);
        Assert.assertNotNull(maximum);
        Assert.assertNotSame(minimum, maximum);
    }

    @Test
    public void testGradientSliderExposesSliderRole() {
        GradientSlider slider = new GradientSlider(GradientSlider.HORIZONTAL);

        AccessibleContext context = slider.getAccessibleContext();
        Assert.assertNotNull(context);
        Assert.assertEquals(AccessibleRole.SLIDER, context.getAccessibleRole());
        Assert.assertSame(context, slider.getAccessibleContext());
    }

    @Test
    public void testGradientSliderNameCanBeSetByCaller() {
        GradientSlider slider = new GradientSlider(GradientSlider.HORIZONTAL);
        slider.getAccessibleContext().setAccessibleName("Element color");

        Assert.assertEquals("Element color", slider.getAccessibleContext().getAccessibleName());
    }

    @Test
    public void testGradientSliderNameComesFromLabelFor() {
        GradientSlider slider = new GradientSlider(GradientSlider.HORIZONTAL);
        JLabel label = new JLabel("Gradient:");
        label.setLabelFor(slider);

        Assert.assertEquals("Gradient:", slider.getAccessibleContext().getAccessibleName());
    }
}
