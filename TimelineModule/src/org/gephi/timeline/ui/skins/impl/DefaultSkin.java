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
package org.gephi.timeline.ui.skins.impl;

import java.awt.BasicStroke;
import java.awt.Stroke;
import org.gephi.timeline.ui.skins.api.TimelineSkin;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;

/**
 * The Default theme.
 *
 * Themes do a bit more that just setting some colors: they can include
 * anything from images to color gradients, shadows or blueprints.

 *
 * @author Julian Bilcke
 */
public class DefaultSkin implements TimelineSkin {

    final static private Color defaultBackgroundColor = new Color (255, 255, 255, 255);
    final static private Color defaultForegroundColor = new Color (55, 55, 55, 255);
    final static private Color defaultBorderColor = new Color (12, 12, 12, 255);

    public Color getDefaultBackgroundColor() {
        return defaultBackgroundColor;
    }

    public Color getDefaultForegroundColor() {
        return defaultForegroundColor;
    }

    public Color getDefaultBorderColor() {
        return defaultBorderColor;
    }


    /**
     * Painters for the backgrounds
     *
     */
    private static class background {

        final static private Color upperColor = new Color (242, 241, 241, 255);
        final static private Color bottomColor = new Color (255, 255, 255, 255);

        final static private Color highlightedUpperColor = new Color (221, 220, 220, 255);
        final static private Color highlightedBottomColor = new Color (255, 255, 255, 255);

    }
    protected Paint backgroundLayerPaint = new GradientPaint(0, 0, background.upperColor, 0, 10, background.bottomColor, true);
    protected Paint highlightedBackgroundLayerPaint = new GradientPaint(0, 0, background.highlightedUpperColor, 0, 10, background.highlightedBottomColor, true);

    /**
     * Builds the background layer paint cache
     *
     * @param width
     * @param height
     */
    public void compileBackgroundLayerPaint(double width, double height) {
        backgroundLayerPaint = new GradientPaint(0, 0, background.upperColor, 0, (int) height, background.bottomColor, true);
        highlightedBackgroundLayerPaint = new GradientPaint(0, 0, background.highlightedUpperColor, 0, (int) height, background.highlightedBottomColor, true);

    }

    /**
     * return the background layer paint
     *
     * @return a Paint representing the background layer color paint
     */
    public Paint getBackgroundLayerPaint() {
        return backgroundLayerPaint;
    }

    public Paint getHighlightedBackgroundLayerPaint() {
        return highlightedBackgroundLayerPaint;
    }


    /**
     * Painters for the data layer
     *
     */
    private static class data {

        final static private Color upperColor = new Color (123, 123, 123, 255);
        final static private Color bottomColor = new Color (220, 220, 220, 255);

        final static private Color highlightedUpperColor = new Color (120, 150, 180, 255);
        final static private Color highlightedBottomColor = new Color (115, 147, 176, 255);

        final static private Stroke defaultStroke = new BasicStroke(1.0f);
        final static private Color defaultStrokeColor = Color.black;

    }
    protected Paint dataLayerPaint = new GradientPaint(0, 0, data.upperColor, 0, 10, data.bottomColor, true);
    protected Paint highlightedDataLayerPaint = new GradientPaint(0, 0, data.highlightedUpperColor, 0, 10, data.highlightedBottomColor, true);

    /**
     * Builds the data layer paint cache
     *
     * @param width
     * @param height
     */
    public void compileDataLayerPaint(double width, double height) {
        dataLayerPaint = new GradientPaint(0, 0, data.upperColor, 0, (int) height, data.bottomColor, true);
        highlightedDataLayerPaint = new GradientPaint(0, 0, data.highlightedUpperColor, 0, (int) height, data.highlightedBottomColor, true);

    }

    public Paint getDataLayerPaint() {
        return dataLayerPaint;
    }

    public Paint getHighlightedDataLayerPaint() {
        return highlightedDataLayerPaint;
    }
    public Stroke getDataLayerStroke() {
        return data.defaultStroke;
    }
    public Color getDataLayerStrokeColor() {
        return data.defaultStrokeColor;
    }






    /**
     * Painters for the selection layer
     *
     */
    private static class selection {

        final static private Color upperColor = new Color (123, 123, 123, 100);
        final static private Color bottomColor = new Color (220, 220, 220, 100);

        final static private Color highlightedUpperColor = new Color (120, 150, 180, 100);
        final static private Color highlightedBottomColor = new Color (115, 147, 176, 100);

        final static private Stroke defaultStroke = new BasicStroke(1.0f);
        final static private Color defaultStrokeColor = Color.black;

    }
    protected Paint selectionLayerPaint = new GradientPaint(0, 0, selection.upperColor, 0, 10, selection.bottomColor, true);
    protected Paint highlightedSelectionLayerPaint = new GradientPaint(0, 0, selection.highlightedUpperColor, 0, 10, selection.highlightedBottomColor, true);

    /**
     * Builds the data layer paint cache
     *
     * @param width
     * @param height
     */
    public void compileSelectionLayerPaint(double width, double height) {
        selectionLayerPaint = new GradientPaint(0, 0, selection.upperColor, 0, (int) height, selection.bottomColor, true);
        highlightedSelectionLayerPaint = new GradientPaint(0, 0, selection.highlightedUpperColor, 0, (int) height, selection.highlightedBottomColor, true);

    }

    public Paint getSelectionLayerPaint() {
        return selectionLayerPaint;
    }

    public Paint getHighlightedSelectionLayerPaint() {
        return highlightedSelectionLayerPaint;
    }
    public Stroke getSelectionLayerStroke() {
        return selection.defaultStroke;
    }
    public Color getSelectionLayerStrokeColor() {
        return selection.defaultStrokeColor;
    }

    protected RenderingHints renderingHints;
    protected Kernel convolutionKernel;
    protected BufferedImageOp blurOperator;

    public DefaultSkin() {
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

    public RenderingHints getRenderingHints() {
        return renderingHints;
    }
}
