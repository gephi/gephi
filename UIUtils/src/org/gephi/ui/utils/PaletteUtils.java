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
public class PaletteUtils {

    public static Palette[] getSequencialPalettes() {
        Palette p1 = new Palette(new Color(0xEDF8FB), new Color(0xB2E2E2), new Color(0x66C2A4), new Color(0x2CA25F), new Color(0x006D2C));
        Palette p2 = new Palette(new Color(0xEDF8FB), new Color(0xB3CDE3), new Color(0x8C96C6), new Color(0x8856A7), new Color(0x810F7C));
        Palette p3 = new Palette(new Color(0xF0F9E8), new Color(0xBAE4BC), new Color(0x7BCCC4), new Color(0x43A2CA), new Color(0x0868AC));
        Palette p4 = new Palette(new Color(0xFEF0D9), new Color(0xFDCC8A), new Color(0xFC8D59), new Color(0xE34A33), new Color(0xB30000));
        Palette p5 = new Palette(new Color(0xFEEBE2), new Color(0xFBB4B9), new Color(0xF768A1), new Color(0xC51B8A), new Color(0x7A0177));
        Palette p6 = new Palette(new Color(0xF1EEF6), new Color(0xBDC9E1), new Color(0x74A9CF), new Color(0x2B8CBE), new Color(0x045A8D));
        Palette p7 = new Palette(new Color(0xFFFFCC), new Color(0xA1DAB4), new Color(0x41B6C4), new Color(0x2C7FB8), new Color(0x253494));
        Palette p8 = new Palette(new Color(0xFFFFD4), new Color(0xFED98E), new Color(0xFE9929), new Color(0xD95F0E), new Color(0x993404));
        return new Palette[]{p1, p2, p3, p4, p5, p6, p7, p8};
    }

    public static Palette[] getDivergingPalettes() {
        Palette p1 = new Palette(new Color(0xA6611A), new Color(0xDFC27D), new Color(0xF5F5F5), new Color(0x80CDC1), new Color(0x018571));
        Palette p2 = new Palette(new Color(0xD01C8B), new Color(0xF1B6DA), new Color(0xF7F7F7), new Color(0xB8E186), new Color(0x4DAC26));
        Palette p3 = new Palette(new Color(0xE66101), new Color(0xFDB863), new Color(0xF7F7F7), new Color(0xB2ABD2), new Color(0x5E3C99));
        Palette p4 = new Palette(new Color(0xCA0020), new Color(0xF4A582), new Color(0xFFFFFF), new Color(0xBABABA), new Color(0x404040));
        Palette p5 = new Palette(new Color(0xD7191C), new Color(0xFDAE61), new Color(0xFFFFBF), new Color(0xABD9E9), new Color(0x2C7BB6));
        return new Palette[]{p1, p2, p3, p4, p5};
    }

    public static Palette[] getQualitativePalettes() {
        Palette p1 = new Palette(new Color(0xA6CEE3), new Color(0x1F78B4), new Color(0xB2DF8A), new Color(0x33A02C), new Color(0xFB9A99), new Color(0xE31A1C), new Color(0xFDBF6F), new Color(0xFF7F00), new Color(0xCAB2D6));
        Palette p2 = new Palette(new Color(0xFBB4AE), new Color(0xB3CDE3), new Color(0xCCEBC5), new Color(0xDECBE4), new Color(0xFED9A6), new Color(0xFFFFCC), new Color(0xE5D8BD), new Color(0xFDDAEC), new Color(0xF2F2F2));
        Palette p3 = new Palette(new Color(0xE41A1C), new Color(0x377EB8), new Color(0x4DAF4A), new Color(0x984EA3), new Color(0xFF7F00), new Color(0xFFFF33), new Color(0xA65628), new Color(0xF781BF), new Color(0x999999));
        Palette p4 = new Palette(new Color(0x8DD3C7), new Color(0xFFFFB3), new Color(0xBEBADA), new Color(0xFB8072), new Color(0x80B1D3), new Color(0xFDB462), new Color(0xB3DE69), new Color(0xFCCDE5), new Color(0xD9D9D9));
        return new Palette[]{p1, p2, p3, p4};
    }

    public static class Palette {

        private Color colors[];

        public Palette(Color... colors) {
            this.colors = colors;
        }

        public Color[] getColors() {
            return colors;
        }
    }
}
