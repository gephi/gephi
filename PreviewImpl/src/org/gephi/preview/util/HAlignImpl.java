/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
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
package org.gephi.preview.util;

import com.itextpdf.text.pdf.PdfContentByte;
import org.gephi.preview.api.util.HAlign;
import processing.core.PApplet;

/**
 * Class providing methods to render an horizontal alignment for different
 * supports.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public enum HAlignImpl implements HAlign {

    LEFT,
    RIGHT;

    public String toCSS() {
        switch (this) {
            case LEFT:
                return "text-anchor: start";
            case RIGHT:
                return "text-anchor: end";
        }

        throw new UnsupportedOperationException();
    }

    public int toProcessing() {
        switch (this) {
            case LEFT:
                return PApplet.LEFT;
            case RIGHT:
                return PApplet.RIGHT;
        }

        throw new UnsupportedOperationException();
    }

    public int toIText() {
        switch (this) {
            case LEFT:
                return PdfContentByte.ALIGN_LEFT;
            case RIGHT:
                return PdfContentByte.ALIGN_RIGHT;
        }

        throw new UnsupportedOperationException();
    }
}
