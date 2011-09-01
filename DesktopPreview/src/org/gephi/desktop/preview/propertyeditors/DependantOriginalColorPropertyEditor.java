/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.preview.propertyeditors;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.preview.types.DependantOriginalColor;

/**
 *
 * @author Mathieu Bastian
 */
public class DependantOriginalColorPropertyEditor extends PropertyEditorSupport {

    @Override
    public Component getCustomEditor() {
        DependantOriginalColorPanel dependantOriginalColorPanel = new DependantOriginalColorPanel();
        dependantOriginalColorPanel.setup(this);
        return dependantOriginalColorPanel;
    }

    @Override
    public String getAsText() {
        DependantOriginalColor c = (DependantOriginalColor) getValue();
        if (c.getMode().equals(DependantOriginalColor.Mode.CUSTOM)) {
            Color color = c.getCustomColor() == null ? Color.BLACK : c.getCustomColor();
            return String.format(
                    "%s [%d,%d,%d]",
                    c.getMode().name().toLowerCase(),
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue());
        } else {
            return c.getMode().name().toLowerCase();
        }

    }

    @Override
    public void setAsText(String s) {

        if (matchColorMode(s, DependantOriginalColor.Mode.CUSTOM.name().toLowerCase())) {
            Pattern p = Pattern.compile("\\w+\\s*\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
            Matcher m = p.matcher(s);
            if (m.lookingAt()) {
                int r = Integer.valueOf(m.group(1));
                int g = Integer.valueOf(m.group(2));
                int b = Integer.valueOf(m.group(3));

                setValue(new DependantOriginalColor(new Color(r, g, b)));
            }
        } else if (matchColorMode(s, DependantOriginalColor.Mode.ORIGINAL.name().toLowerCase())) {
            setValue(new DependantOriginalColor(DependantOriginalColor.Mode.ORIGINAL));
        } else if (matchColorMode(s, DependantOriginalColor.Mode.PARENT.name().toLowerCase())) {
            setValue(new DependantOriginalColor(DependantOriginalColor.Mode.PARENT));
        }
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    private boolean matchColorMode(String s, String identifier) {
        String regexp = String.format("\\s*%s\\s*", identifier);
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(s);
        return m.lookingAt();
    }
}
