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
package org.gephi.preview.types;

import java.awt.Color;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeColor {

    public enum Mode {

        SOURCE, TARGET, MIXED, CUSTOM, ORIGINAL
    };
    private Color customColor = Color.BLACK;
    private final Mode mode;

    public EdgeColor(Mode mode) {
        customColor = null;
        this.mode = mode;
    }

    public EdgeColor(Color customColor) {
        this.customColor = customColor;
        this.mode = Mode.CUSTOM;
    }

    public Mode getMode() {
        return mode;
    }

    public Color getCustomColor() {
        return customColor;
    }

    public Color getColor(Color edgeColor, Color sourceColor, Color targetColor) {
        switch (mode) {
            case ORIGINAL:
                return edgeColor != null ? edgeColor : Color.BLACK;
            case SOURCE:
                return sourceColor;
            case TARGET:
                return targetColor;
            case MIXED:
                return new Color((int) ((sourceColor.getRed() + targetColor.getRed()) / 2f),
                        (int) ((sourceColor.getGreen() + targetColor.getGreen()) / 2f),
                        (int) ((sourceColor.getBlue() + targetColor.getBlue()) / 2f),
                        (int) ((sourceColor.getAlpha() + targetColor.getAlpha()) / 2f));
            case CUSTOM:
                return customColor;
        }
        return null;
    }
}
