/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.ui.timeline.plugin.drawers.minimal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Stroke;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jbilcke
 */
public class MinimalDrawerSettings {

    public RenderingHints renderingHints;
    public Kernel convolutionKernel;
    public ConvolveOp blurOperator;
    private int lastWidth = 0;
    private int lastHeight = 0;

    public class Background {

        public Color top;
        public Color bottom;
        public Paint paint;
    }
    public Background background = new Background();

    public class SelectionBox {

        public Color top;
        public Color bottom;
        public Paint paint;
        public int visibleHookWidth; // the "visible hook" (mouse hook, to move the selection box)
        public int invisibleHookMargin; // let the "invisible hook" be a bit larger on the left..
        public int minimalWidth;

        public Color mouseOverTopColor;
        public Color activatedTopColor;
        public Color mouseOverBottomColor;
        public Color activatedBottomColor;
        public Paint mouseOverPaint;
        public Paint activatedPaint;
    }
    public SelectionBox selection = new SelectionBox();


    public class Informations {

        public int fontSize;
        public Font font;
        public FontMetrics fontMetrics;
        public Color fontColor;

        public int topMargin;
        public int leftMargin;
        public int textTopPosition;
        public int textBottomMargin;
    }
    public Informations informations = new Informations();
    public Stroke defaultStroke;
    public Color defaultStrokeColor;
    public Color shadowColor;
    public int hookLength;

    void update(int width, int height) {
        if (lastWidth == width && lastHeight == height) {
            return;
        }
        lastWidth = width;
        lastHeight = height;

        background.paint = new GradientPaint(0, 0, background.top, 0, height, background.bottom, true);
        selection.paint = new GradientPaint(0, 0, selection.top, 0, height, selection.bottom, true);
        selection.mouseOverPaint = new GradientPaint(0, 0, selection.mouseOverTopColor, 0, height, selection.mouseOverBottomColor, true);
        selection.activatedPaint = new GradientPaint(0, 0, selection.activatedTopColor, 0, height, selection.activatedBottomColor, true);
    }

    public MinimalDrawerSettings() {
        /* DEFINE THEME HERE */
        //background.top = new Color(101, 101, 101, 255);
        //background.bottom = new Color(47, 45, 43, 255);
        //background.top = new Color(131, 131, 131, 255);
        //background.bottom = new Color(77, 75, 73, 255);
        background.top = new Color(151, 151, 151, 255);
        background.bottom = new Color(97, 95, 93, 255);
        background.paint = new GradientPaint(0, 0, background.top, 0, 20, background.bottom, true);

        //selection.top = new Color(89, 161, 235, 153);
        //selection.bottom = new Color(37, 104, 161, 153);
        selection.top = new Color(108, 151, 194, 255);
        selection.bottom = new Color(57, 97, 131, 255);
        selection.paint = new GradientPaint(0, 0, selection.top, 0, 20, selection.bottom, true);
        selection.visibleHookWidth = 12; // the "visible hook" (mouse hook, to move the selection box)
        selection.invisibleHookMargin = 3; // let the "invisible hook" be a bit larger on the left..
        selection.minimalWidth = 16;
        selection.mouseOverTopColor = new Color(102,195, 145, 255);
        selection.activatedTopColor = new Color(188, 118, 114, 255);
        selection.mouseOverBottomColor = new Color(60, 143, 96, 255);
        selection.activatedBottomColor = new Color(151, 79, 79, 255);
        selection.mouseOverPaint = new GradientPaint(0, 0, selection.mouseOverTopColor, 0, 20, selection.mouseOverBottomColor, true);
        selection.activatedPaint = new GradientPaint(0, 0, selection.activatedTopColor, 0, 20, selection.activatedBottomColor, true);

        informations.fontColor = new Color(235, 235, 235, 255);
        shadowColor = new Color(35, 35, 35, 105);

        defaultStroke = new BasicStroke(1.0f);
        defaultStrokeColor = Color.black;

        informations.fontSize = 12;
        informations.font = new Font("DejaVu Sans Mono", 0, informations.fontSize);
        informations.fontMetrics = new FontMetrics(informations.font) {
        };
        informations.topMargin = 2;
        informations.leftMargin = 2;
        informations.textTopPosition = informations.topMargin + informations.fontSize;
        informations.textBottomMargin = 3;

        hookLength = 16;



        //System.out.println("Generating filters for " + this);
        // filters
        Map<Key, Object> map = new HashMap<Key, Object>();
        // bilinear
        map.put(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        map.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        // Antialiasing (text and image)
        map.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        map.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        renderingHints = new RenderingHints(map);

        float ninth = 1.0f / 9.0f;
        float[] blurKernel = {ninth, ninth, ninth, ninth, ninth, ninth, ninth,
            ninth, ninth};
        convolutionKernel = new Kernel(3, 3, blurKernel);
        blurOperator = new ConvolveOp(convolutionKernel, ConvolveOp.EDGE_NO_OP,
                renderingHints);
    }
}
