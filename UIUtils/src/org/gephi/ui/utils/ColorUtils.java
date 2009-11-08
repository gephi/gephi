/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.utils;

import java.awt.Color;

/**
 *
 * @author Mathieu Bastian
 */
public class ColorUtils {

    public static String encode(Color color) {
        char[] buf = new char[8];
        String s = Integer.toHexString(color.getRed());
        if (s.length() == 1) {
            buf[0] = '0';
            buf[1] = s.charAt(0);
        } else {
            buf[0] = s.charAt(0);
            buf[1] = s.charAt(1);
        }
        s = Integer.toHexString(color.getGreen());
        if (s.length() == 1) {
            buf[2] = '0';
            buf[3] = s.charAt(0);
        } else {
            buf[2] = s.charAt(0);
            buf[3] = s.charAt(1);
        }
        s = Integer.toHexString(color.getBlue());
        if (s.length() == 1) {
            buf[4] = '0';
            buf[5] = s.charAt(0);
        } else {
            buf[4] = s.charAt(0);
            buf[5] = s.charAt(1);
        }
        s = Integer.toHexString(color.getAlpha());
        if (s.length() == 1) {
            buf[6] = '0';
            buf[7] = s.charAt(0);
        } else {
            buf[6] = s.charAt(0);
            buf[7] = s.charAt(1);
        }
        return String.valueOf(buf);
    }

    public static Color decode(String nm) throws NumberFormatException {
        int i = (int) Long.parseLong(nm, 16); //Bug 4215269
        return new Color((i >> 24) & 0xFF, (i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }

    public static Color decode(float[] array) {
        if (array.length == 3) {
            return new Color(array[0], array[1], array[2]);
        } else if (array.length == 4) {
            return new Color(array[0], array[1], array[2], array[3]);
        } else {
            throw new IllegalArgumentException("Must be a 3 or 4 length array");
        }
    }
}
