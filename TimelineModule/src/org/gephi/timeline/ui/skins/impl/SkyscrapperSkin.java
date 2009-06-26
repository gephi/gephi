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

import org.gephi.timeline.ui.skins.api.TimelineSkin;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

/**
 * skyscrapper theme - blue sky background, grey metallic foreground
 *
 * Themes do a bit more that just setting some colors: they can include
 * anything from images to color gradients, shadows or blueprints.

 *
 * @author Julian Bilcke
 */
public class SkyscrapperSkin extends DefaultSkin {

    final static private Color defaultBackgroundColor = new Color (255, 255, 255, 0);
    final static private Color defaultForegroundColor = new Color (55, 55, 55, 20);
    final static private Color defaultBorderColor = new Color (12, 12, 12, 0);

    private static class background {

        final static private Color upperColor = new Color (242, 241, 241, 0);
        final static private Color bottomColor = new Color (255, 255, 255, 0);

        final static private Color highlightedUpperColor = new Color (221, 220, 220, 0);
        final static private Color highlightedBottomColor = new Color (255, 255, 255, 0);

    }
    private Paint backgroundLayerPaint = new GradientPaint(0, 0, background.upperColor, 1, 10, background.bottomColor, true);
    private Paint highlightedBackgroundLayerPaint = new GradientPaint(0, 0, background.highlightedUpperColor, 1, 10, background.highlightedBottomColor, true);

    public void compileBackgroundLayerPaint(double width, double height) {
        backgroundLayerPaint = new GradientPaint(0, 0, background.upperColor, 1, (int) height, background.bottomColor, true);
        highlightedBackgroundLayerPaint = new GradientPaint(0, 0, background.highlightedUpperColor, 1, (int) height, background.highlightedBottomColor, true);

    }

    private static class data {

        final static private Color upperColor = new Color (220, 220, 220, 0);
        final static private Color bottomColor = new Color (123, 123, 123, 0);

        final static private Color highlightedUpperColor = new Color (120, 150, 180, 0);
        final static private Color highlightedBottomColor = new Color (115, 147, 176, 0);

    }
    private Paint dataLayerPaint = new GradientPaint(0, 0, data.upperColor, 1, 10, data.bottomColor, true);
    private Paint highlightedDataLayerPaint = new GradientPaint(0, 0, data.highlightedUpperColor, 1, 10, data.highlightedBottomColor, true);


    public void compileDataLayerPaint(double width, double height) {
        dataLayerPaint = new GradientPaint(0, 0, data.upperColor, 1, (int) height, data.bottomColor, true);
        highlightedDataLayerPaint = new GradientPaint(0, 0, data.highlightedUpperColor, 1, (int) height, data.highlightedBottomColor, true);
    }

}
