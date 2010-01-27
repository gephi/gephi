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

    public class Background {
        public Color top;
        public Color bottom;
        public Paint paint;
    }
    public Background background = new Background();
    
    public class Informations {
            public Font font;
            public FontMetrics fontMetrics;
            public Color fontColor;
            public Color fontShadow;
    }
    public Informations informations = new Informations();
    
    public Stroke defaultStroke;
    public Color defaultStrokeColor;

    public int hookLength;

    public MinimalDrawerSettings() {
        /* DEFINE THEME HERE */
        background.top = new Color(101, 101, 101, 255);
        background.bottom = new Color(47, 45, 43, 255);
        background.paint = new GradientPaint(0, 0, background.top, 0, 10, background.bottom, true);

        informations.fontColor = new Color(235,235,235,255);
        informations.fontShadow = new Color(35,35,35,255);
        defaultStroke = new BasicStroke(1.0f);
        defaultStrokeColor = Color.black;

        informations.font = new Font("DejaVu Sans Mono", 0, 12);
        informations.fontMetrics = new FontMetrics(informations.font) {};

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
