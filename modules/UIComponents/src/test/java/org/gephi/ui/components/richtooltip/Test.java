package org.gephi.ui.components.richtooltip;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedString;
import org.pushingpixels.flamingo.internal.utils.FlamingoUtilities;

public class Test {

  @org.junit.Test
  public void test() {
    String desc = "Quality layout: a linear-attraction linear-repulsion model with few approximations (BarnesHut). Speed automatically computed.\n";

    RichTooltip tooltip = new RichTooltip("foo", desc);
    JRichTooltipPanel parent = new JRichTooltipPanel(tooltip);

    Font font = FlamingoUtilities.getFont(parent, "Ribbon.font",
        "Button.font", "Panel.font");
    AttributedString attributedDescription = new AttributedString(
        desc);
    attributedDescription.addAttribute(TextAttribute.FONT, font);
    FontRenderContext frc = new FontRenderContext(
        new AffineTransform(), true, false);

    LineBreakMeasurer lineBreakMeasurer = new LineBreakMeasurer(
        attributedDescription.getIterator(), frc);

    TextLayout tl = lineBreakMeasurer
        .nextLayout(125);
  }
}
