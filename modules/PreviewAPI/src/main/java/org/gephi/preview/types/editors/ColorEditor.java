package org.gephi.preview.types.editors;

import java.awt.Color;
import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        Color color = (Color) getValue();
        return String.format(
            "[%d,%d,%d]",
            color.getRed(),
            color.getGreen(),
            color.getBlue());
    }

    @Override
    public void setAsText(String s) {
        Pattern p = Pattern.compile("\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
        Matcher m = p.matcher(s);
        if (m.lookingAt()) {
            int r = Integer.parseInt(m.group(1));
            int g = Integer.parseInt(m.group(2));
            int b = Integer.parseInt(m.group(3));

            setValue(new Color(r, g, b));
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return false;
    }
}