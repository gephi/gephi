package org.gephi.preview.plugin.renderers;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

public class PDFUtils {

    public static void drawCircle(PDPageContentStream stream, final float x, final float y, final float r)
        throws IOException {
        float b = 0.5523f;
        stream.moveTo(x + r, y);
        stream.curveTo(x + r, y + r * b, x + r * b, y + r, x, y + r);
        stream.curveTo(x - r * b, y + r, x - r, y + r * b, x - r, y);
        stream.curveTo(x - r, y - r * b, x - r * b, y - r, x, y - r);
        stream.curveTo(x + r * b, y - r, x + r, y - r * b, x + r, y);
    }

    public static void drawArc(PDPageContentStream stream, final float x1, final float y1, final float x2, final float y2,
                        final float startAng, final float extent) throws IOException {
        List<float[]> ar = bezierArc(x1, y1, x2, y2, startAng, extent);
        if (ar.isEmpty()) {
            return;
        }
        float[] pt = ar.get(0);
        stream.moveTo(pt[0], pt[1]);
        for (float[] floats : ar) {
            pt = floats;
            stream.curveTo(pt[2], pt[3], pt[4], pt[5], pt[6], pt[7]);
        }
    }

    private static List<float[]> bezierArc(float x1, float y1, float x2, float y2, final float startAng,
                                           final float extent) {
        float tmp;
        if (x1 > x2) {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        if (y2 > y1) {
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }

        float fragAngle;
        int Nfrag;
        if (Math.abs(extent) <= 90f) {
            fragAngle = extent;
            Nfrag = 1;
        } else {
            Nfrag = (int) Math.ceil(Math.abs(extent) / 90f);
            fragAngle = extent / Nfrag;
        }
        float x_cen = (x1 + x2) / 2f;
        float y_cen = (y1 + y2) / 2f;
        float rx = (x2 - x1) / 2f;
        float ry = (y2 - y1) / 2f;
        float halfAng = (float) (fragAngle * Math.PI / 360.);
        float kappa = (float) Math.abs(4. / 3. * (1. - Math.cos(halfAng)) / Math.sin(halfAng));
        List<float[]> pointList = new ArrayList<>();
        for (int i = 0; i < Nfrag; ++i) {
            float theta0 = (float) ((startAng + i * fragAngle) * Math.PI / 180.);
            float theta1 = (float) ((startAng + (i + 1) * fragAngle) * Math.PI / 180.);
            float cos0 = (float) Math.cos(theta0);
            float cos1 = (float) Math.cos(theta1);
            float sin0 = (float) Math.sin(theta0);
            float sin1 = (float) Math.sin(theta1);
            if (fragAngle > 0f) {
                pointList.add(new float[] {x_cen + rx * cos0,
                    y_cen - ry * sin0,
                    x_cen + rx * (cos0 - kappa * sin0),
                    y_cen - ry * (sin0 + kappa * cos0),
                    x_cen + rx * (cos1 + kappa * sin1),
                    y_cen - ry * (sin1 - kappa * cos1),
                    x_cen + rx * cos1,
                    y_cen - ry * sin1});
            } else {
                pointList.add(new float[] {x_cen + rx * cos0,
                    y_cen - ry * sin0,
                    x_cen + rx * (cos0 + kappa * sin0),
                    y_cen - ry * (sin0 - kappa * cos0),
                    x_cen + rx * (cos1 - kappa * sin1),
                    y_cen - ry * (sin1 + kappa * cos1),
                    x_cen + rx * cos1,
                    y_cen - ry * sin1});
            }
        }
        return pointList;
    }

    public static float getTextHeight(PDFont pdFont, float fontSize) throws IOException {
        return pdFont.getFontDescriptor().getCapHeight() / 1000 * fontSize;
    }

    public static float getTextWidth(PDFont pdFont, float fontSize, String text) throws IOException {
        return pdFont.getStringWidth(text) / 1000 * fontSize;
    }

}
