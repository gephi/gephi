/*
Copyright 2008-2011 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>, Mathieu Bastian
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
package org.gephi.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.RenderingHints;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.openide.util.Lookup;
import processing.core.PGraphics;
import processing.core.PGraphicsJava2D;
import processing.core.PVector;

/**
 *
 * @author Mathieu Bastian
 */
public class ProcessingGraphics extends PGraphicsJava2D {

    private final PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
    private PreviewModel model;
    private RenderTarget target;
    //Drawing
    private final PVector ref = new PVector();
    private final PVector trans = new PVector();
    private final PVector lastMove = new PVector();
    private float scaling;
    private Color background = Color.WHITE;

    public ProcessingGraphics(int width, int height) {
        setSize(width, height);
    }

    public void refresh(PreviewModel previewModel, RenderTarget target) {
        this.model = previewModel;
        this.target = target;
        if (model != null) {
            background = model.getProperties().getColorValue(PreviewProperty.BACKGROUND_COLOR);
            initAppletLayout();

            beginDraw();
            smooth();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            rectMode(PGraphics.CENTER);
            if (background != null) {
                background(background.getRed(), background.getGreen(), background.getBlue());
            }
            
            // user zoom
            PVector center = new PVector(width / 2f, height / 2f);
            PVector scaledCenter = PVector.mult(center, scaling);
            PVector scaledTrans = PVector.sub(center, scaledCenter);
            translate(scaledTrans.x, scaledTrans.y);
            scale(scaling);

            // user move
            translate(trans.x, trans.y);

            //Draw target
            previewController.render(target);

            endDraw();
        }
    }

    /**
     * Initializes the preview applet layout according to the graph's dimension.
     */
    private void initAppletLayout() {
//            graphSheet.setMargin(MARGIN);
        if (model != null && model.getDimensions() != null && model.getTopLeftPosition() != null) {

            // initializes zoom
            Dimension dimensions = model.getDimensions();
            Point topLeftPostition = model.getTopLeftPosition();
            PVector box = new PVector((float) dimensions.getWidth(), (float) dimensions.getHeight());
            float ratioWidth = width / box.x;
            float ratioHeight = height / box.y;
            scaling = ratioWidth < ratioHeight ? ratioWidth : ratioHeight;

            // initializes move
            PVector semiBox = PVector.div(box, 2);
            PVector topLeftVector = new PVector((float) topLeftPostition.x, (float) topLeftPostition.y);
            PVector center = new PVector(width / 2f, height / 2f);
            PVector scaledCenter = PVector.add(topLeftVector, semiBox);
            trans.set(center);
            trans.sub(scaledCenter);
            lastMove.set(trans);
        }
    }
}
