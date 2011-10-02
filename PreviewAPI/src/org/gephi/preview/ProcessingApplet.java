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
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.openide.util.Lookup;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PVector;

/**
 *
 * @author Jeremy Subtil, Mathieu Bastian
 */
public class ProcessingApplet extends PApplet implements MouseWheelListener {

    //Const
    private static final int WHEEL_TIMER = 500;
    private final static float MARGIN = 10f;
    //States
    private final PVector ref = new PVector();
    private final PVector trans = new PVector();
    private final PVector lastMove = new PVector();
    private float scaling;
    private Color background = Color.WHITE;
    private Timer wheelTimer;
    //Data
    private final PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
    private PreviewModel model;
    private RenderTarget target;
    //Caching
    private final HashMap<String, PFont> fontMap = new HashMap<String, PFont>();

    /**
     * Refreshes the preview using the current graph from the preview
     * controller.
     */
    public void refresh(PreviewModel model, RenderTarget target) {
        this.model = model;
        this.target = target;
        // updates fonts
        //fontMap.clear(); Don't clear to prevent PFont memory leak from Processing library.
        if (model != null) {
            background = model.getProperties().getColorValue(PreviewProperty.BACKGROUND_COLOR);
        }

        // redraws the applet
        initAppletLayout();
        redraw();
    }

    public boolean isRedrawn() {
        return redraw;
    }

    @Override
    public void setup() {
        size(1000, 1000, JAVA2D);
        rectMode(CENTER);
        background(background.getRGB());
        smooth();
        noLoop(); // the preview is drawn once and then redrawn when necessary
        addMouseWheelListener(this);
    }

    @Override
    public void draw() {
        // blank the applet
        background(background.getRGB());

        // user zoom
        PVector center = new PVector(width / 2f, height / 2f);
        PVector scaledCenter = PVector.mult(center, scaling);
        PVector scaledTrans = PVector.sub(center, scaledCenter);
        translate(scaledTrans.x, scaledTrans.y);
        scale(scaling);
//        scale(1f, -1f);

        // user move
        translate(trans.x, trans.y);

        //Draw target
        previewController.render(target);
    }

    @Override
    protected void resizeRenderer(int i, int i1) {
        if (i > 0 && i1 > 0) {
            super.resizeRenderer(i, i1);
        }
    }

    @Override
    public void mousePressed() {
        ref.set(mouseX, mouseY, 0);
    }

    @Override
    public void mouseDragged() {
        setMoving(true);
        trans.set(mouseX, mouseY, 0);
        trans.sub(ref);
        trans.div(scaling); // ensure const. moving speed whatever the zoom is
        trans.add(lastMove);
        redraw();
    }

    @Override
    public void mouseReleased() {
        lastMove.set(trans);
        setMoving(false);
        redraw();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getUnitsToScroll() == 0) {
            return;
        }
        float way = -e.getUnitsToScroll() / Math.abs(e.getUnitsToScroll());
        scaling = scaling * (way > 0 ? 2f : 0.5f);
        setMoving(true);
        if (wheelTimer != null) {
            wheelTimer.cancel();
            wheelTimer = null;
        }
        wheelTimer = new Timer();
        wheelTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                setMoving(false);
                redraw();
                wheelTimer = null;
            }
        }, WHEEL_TIMER);

        redraw();
    }

    @Override
    public void keyPressed() {
        switch (key) {
            case '+':
                scaling = scaling * 2f;
                break;
            case '-':
                scaling = scaling / 2f;
                break;
            case '0':
                scaling = 1;
                break;
        }

        redraw();
    }

    public void zoomPlus() {
        scaling = scaling * 2f;
        redraw();
    }

    public void zoomMinus() {
        scaling = scaling / 2f;
        redraw();
    }

    public void resetZoom() {
        scaling = 0;
        initAppletLayout();
        redraw();
    }

    public void setMoving(boolean moving) {
        if (model != null) {
            model.getProperties().putValue(PreviewProperty.MOVING, moving);
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
            if (scaling == 0) {
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

    /**
     * Creates a Processing font from a classic font.
     *
     * @param font  a font to transform
     * @return      a Processing font
     */
    private PFont createFont(Font font) {
        return createFont(font.getName(), 1);
    }

    /**
     * Returns the Processing font related to the given classic font.
     *
     * @param font  a classic font
     * @return      the related Processing font
     */
    private PFont getPFont(Font font) {
        String fontName = font.getName();
        if (fontMap.containsKey(fontName)) {
            return fontMap.get(fontName);
        }

        PFont pFont = createFont(font);
        fontMap.put(fontName, pFont);
        return pFont;
    }
}