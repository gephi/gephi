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
        char[] buf = new char[7];
        buf[0] = '#';
        String s = Integer.toHexString(color.getRed());
        if (s.length() == 1) {
            buf[1] = '0';
            buf[2] = s.charAt(0);
        } else {
            buf[1] = s.charAt(0);
            buf[2] = s.charAt(1);
        }
        s = Integer.toHexString(color.getGreen());
        if (s.length() == 1) {
            buf[3] = '0';
            buf[4] = s.charAt(0);
        } else {
            buf[3] = s.charAt(0);
            buf[4] = s.charAt(1);
        }
        s = Integer.toHexString(color.getBlue());
        if (s.length() == 1) {
            buf[5] = '0';
            buf[6] = s.charAt(0);
        } else {
            buf[5] = s.charAt(0);
            buf[6] = s.charAt(1);
        }
        return String.valueOf(buf);
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
