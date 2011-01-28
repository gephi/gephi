/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.utils;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

//From Netbeans Profiler sources - ProfilerControlPanel2
//Tomas Hurka
//Ian Formanek
public final class WhiteFilter extends RGBImageFilter {
    //~ Instance fields ------------------------------------------------------------------------------------------------------

    private final float[] hsv = new float[3];

    //~ Constructors ---------------------------------------------------------------------------------------------------------
    /**
     * Constructs a GrayFilter object that filters a color image to a
     * grayscale image. Used by buttons to create disabled ("grayed out")
     * button images.
     */
    public WhiteFilter() {
        // canFilterIndexColorModel indicates whether or not it is acceptable
        // to apply the color filtering of the filterRGB method to the color
        // table entries of an IndexColorModel object in lieu of pixel by pixel
        // filtering.
        canFilterIndexColorModel = true;
    }

    //~ Methods --------------------------------------------------------------------------------------------------------------
    /**
     * Creates a disabled image
     */
    public static Image createDisabledImage(final Image i) {
        final WhiteFilter filter = new WhiteFilter();
        final ImageProducer prod = new FilteredImageSource(i.getSource(), filter);

        return Toolkit.getDefaultToolkit().createImage(prod);
    }

    /**
     * Overrides <code>RGBImageFilter.filterRGB</code>.
     */
    public int filterRGB(final int x, final int y, final int rgb) {
        int transparency = (rgb >> 24) & 0xFF;

        if (transparency <= 1) {
            return rgb; // do not alter fully transparent pixels (those would end up being black)
        }

        transparency /= 2; // set transparency to 50% of original
        Color.RGBtoHSB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, (rgb >> 0) & 0xFF, hsv);
        hsv[1] = 0;

        return Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]) + (transparency << 24);
    }
}

