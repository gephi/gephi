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

package org.gephi.timeline.ui.skins.api;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 *
 * @author Julian Bilcke
 */
public interface TimelineSkin {

    public FontMetrics getDataLayerFontMetrics();

   /**
     * return the default background color
     *
     * @return a Color representing the default background color
     */
    public Color getDefaultBackgroundColor();

   /**
     * return the default foreground color
     *
     * @return a Color representing the default foreground color
     */
    public Color getDefaultForegroundColor();

   /**
     * return the default border color
     *
     * @return a Color representing the default border color
     */
    public Color getDefaultBorderColor();

    /**
     * Builds the background layer paint cache
     *
     * @param width
     * @param height
     */
    public void compileBackgroundLayerPaint(double width, double height);

   /**
     * return the background layer paint
     *
     * @return a Paint representing the background layer color paint
     */
    public Paint getBackgroundLayerPaint();

    /**
     * return the highlighted background layer paint
     *
     * @return a Paint representing the highlighted background layer color paint
     */
    public Paint getHighlightedBackgroundLayerPaint();

    /**
     * Builds the data layer paint cache
     *
     * @param width
     * @param height
     */
    public void compileDataLayerPaint(double width, double height);

   /**
     * return the data layer paint
     *
     * @return a Paint representing the data layer color paint
     */
    public Paint getDataLayerPaint();

    /**
     * return the highlighted data layer paint
     *
     * @return a Paint representing the highlighted data layer color paint
     */
    public Paint getHighlightedDataLayerPaint();

    public Stroke getDataLayerStroke();

    public Color getDataLayerStrokeColor();

    public Font getDataLayerFont();

       /**
     * Builds the data layer paint cache
     *
     * @param width
     * @param height
     */
    public void compileSelectionLayerPaint(double width, double height);

    
   /**
     * return the selection layer paint
     *
     * @return a Paint representing the selection layer color paint
     */
    public Paint getSelectionLayerPaint();

    /**
     * return the highlighted selection layer paint
     *
     * @return a Paint representing the highlighted selection layer color paint
     */
    public Paint getHighlightedSelectionLayerPaint();

    public Stroke getSelectionLayerStroke();

    public Color getSelectionLayerStrokeColor();

    public int getSelectionHookSideLength();

    public RenderingHints getRenderingHints();
}