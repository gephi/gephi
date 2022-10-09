package org.gephi.preview.types.editors;

import java.awt.Color;
import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractColorPropertyEditor extends PropertyEditorSupport {

    protected String toText(String mode, Color color) {
        if (color.getAlpha() < 255) {
            return String.format(
                "%s [%d,%d,%d,%d]",
                mode.toLowerCase(),
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                color.getAlpha());
        } else {
            return String.format(
                "%s [%d,%d,%d]",
                mode.toLowerCase(),
                color.getRed(),
                color.getGreen(),
                color.getBlue());
        }
    }

    protected Color toColor(String text) {
        Pattern p = Pattern.compile("\\w+\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,?(\\d+)?\\s*\\]");
        Matcher m = p.matcher(text);
        if (m.lookingAt()) {
            int r = Integer.valueOf(m.group(1));
            int g = Integer.valueOf(m.group(2));
            int b = Integer.valueOf(m.group(3));
            String alpha = m.group(4);
            if (alpha != null) {
                int a = Integer.valueOf(alpha);
                return new Color(r, g, b, a);
            } else {
                return new Color(r, g, b);
            }
        }
        return Color.BLACK;
    }

    protected boolean matchColorMode(String s, String identifier) {
        String regexp = String.format("\\s*%s\\s*", identifier);
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        return m.lookingAt();
    }
}
