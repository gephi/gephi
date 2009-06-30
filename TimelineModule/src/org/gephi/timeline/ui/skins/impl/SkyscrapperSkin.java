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
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Stroke;

/**
 * skyscrapper theme - blue sky background, grey metallic foreground
 *
 * Themes do a bit more that just setting some colors: they can include
 * anything from images to color gradients, shadows or blueprints.

 *
 * @author Julian Bilcke
 */
public class SkyscrapperSkin extends DefaultSkin {

    final static private Color defaultBackgroundColor = new Color(255, 255, 255, 255);
    final static private Color defaultForegroundColor = new Color(55, 55, 55, 255);
    final static private Color defaultBorderColor = new Color(12, 12, 12, 255);

    private static class background {

        final static private Color upperColor = new Color(242, 241, 241, 255);
        final static private Color bottomColor = new Color(255, 255, 255, 255);
        final static private Color highlightedUpperColor = new Color(221, 220, 220, 255);
        final static private Color highlightedBottomColor = new Color(255, 255, 255, 255);
    }

    @Override
    public void compileBackgroundLayerPaint(double width, double height) {
        backgroundLayerPaint = new GradientPaint(0, 0, background.upperColor, 0, (int) height, background.bottomColor, true);
        highlightedBackgroundLayerPaint = new GradientPaint(0, 0, background.highlightedUpperColor, 0, (int) height, background.highlightedBottomColor, true);
    }

    private static class data {

        final static private Color upperColor = new Color(123, 123, 123, 255);
        final static private Color bottomColor = new Color(220, 220, 220, 255);
        final static private Color highlightedUpperColor = new Color(120, 150, 180, 255);
        final static private Color highlightedBottomColor = new Color(115, 147, 176, 255);
        final static private Stroke defaultStroke = new BasicStroke(1.0f);
        final static private Color defaultStrokeColor = Color.black;
    }

    @Override
    public void compileDataLayerPaint(double width, double height) {
        dataLayerPaint = new GradientPaint(0, 0, data.upperColor, 0, (int) height, data.bottomColor, true);
        highlightedDataLayerPaint = new GradientPaint(0, (int) height, data.highlightedUpperColor, 0, (int) height, data.highlightedBottomColor, true);
    }

    /**
     * Painters for the selection layer
     *
     */
    private static class selection {

        final static private Color upperColor = new Color(123, 123, 123, 255);
        final static private Color bottomColor = new Color(220, 220, 220, 255);
        final static private Color highlightedUpperColor = new Color(120, 150, 180, 255);
        final static private Color highlightedBottomColor = new Color(115, 147, 176, 255);
        final static private Stroke defaultStroke = new BasicStroke(1.0f);
        final static private Color defaultStrokeColor = Color.black;
    }

    public void compileSelectionLayerPaint(double width, double height) {
        selectionLayerPaint = new GradientPaint(0, 0, selection.upperColor, 0, (int) height, selection.bottomColor, true);
        highlightedSelectionLayerPaint = new GradientPaint(0, 0, selection.highlightedUpperColor, 0, (int) height, selection.highlightedBottomColor, true);
    }
}
