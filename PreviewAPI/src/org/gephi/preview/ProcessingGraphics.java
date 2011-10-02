/*
Copyright 2008-2011 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>, Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
