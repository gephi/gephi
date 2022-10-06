package org.gephi.preview.plugin.renderers;


import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class PDFUtils {

    public static void drawCircle(PDPageContentStream stream, final float x, final float y, final float r) throws IOException {
        float b = 0.5523f;
        stream.moveTo(x + r, y);
        stream.curveTo(x + r, y + r * b, x + r * b, y + r, x, y + r);
        stream.curveTo(x - r * b, y + r, x - r, y + r * b, x - r, y);
        stream.curveTo(x - r, y - r * b, x - r * b, y - r, x, y - r);
        stream.curveTo(x + r * b, y - r, x + r, y - r * b, x + r, y);
    }

    public static float getTextHeight(PDFont pdFont, float fontSize) throws IOException {
        return pdFont.getFontDescriptor().getCapHeight() / 1000 * fontSize;
    }

    public static float getTextWidth(PDFont pdFont, float fontSize, String text) throws IOException {
        return pdFont.getStringWidth(text) / 1000 * fontSize;
    }

}
