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
