/*
Copyright 2008-2011 Gephi
Authors : Julian Bilcke
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
package org.gephi.desktop.timeline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Stroke;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;

/**
 *
 * @author Julian Bilcke
 */
public class DrawerSettings {

    public class Background {

        public Color top;
        public Color bottom;
        public Paint paint;
    }

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
    public Background background = new Background();
    public SelectionBox selection = new SelectionBox();
    public Stroke defaultStroke;
    public Color defaultStrokeColor;
    public Color shadowColor;
    public int hookLength;
    public RenderingHints renderingHints;
    public Kernel convolutionKernel;
    public ConvolveOp blurOperator;
    private int lastWidth = 0;
    private int lastHeight = 0;
    public int tmMarginTop;
    public int tmMarginBottom;
    public int topChartMargin;

    void update(int width, int height) {
        if (lastWidth == width && lastHeight == height) {
            return;
        }
        lastWidth = width;
        lastHeight = height;

//        background.paint = new GradientPaint(0, 0, background.top, 0, height, background.bottom, true);
        selection.paint = new GradientPaint(0, 0, selection.top, 0, height, selection.bottom, true);
        selection.mouseOverPaint = new GradientPaint(0, 0, selection.mouseOverTopColor, 0, height, selection.mouseOverBottomColor, true);
        selection.activatedPaint = new GradientPaint(0, 0, selection.activatedTopColor, 0, height, selection.activatedBottomColor, true);
    }

    public DrawerSettings() {
        /* DEFINE THEME HERE */
        //background.top = new Color(101, 101, 101, 255);
        //background.bottom = new Color(47, 45, 43, 255);
        //background.top = new Color(131, 131, 131, 255);
        //background.bottom = new Color(77, 75, 73, 255);
//        background.top = new Color(151, 151, 151, 0);
        background.top = UIManager.getColor("NbExplorerView.background");
        background.bottom = new Color(97, 95, 93, 0);
//        background.paint = new GradientPaint(0, 0, background.top, 0, 20, background.bottom, true);

        //selection.top = new Color(89, 161, 235, 153);
        //selection.bottom = new Color(37, 104, 161, 153);
        selection.top = new Color(108, 151, 194, 50);
        selection.bottom = new Color(57, 97, 131, 50);
        selection.paint = new GradientPaint(0, 0, selection.top, 0, 20, selection.bottom, true);
        selection.visibleHookWidth = 6; // the "visible hook" (mouse hook, to move the selection box)
        selection.invisibleHookMargin = 1; // let the "invisible hook" be a bit larger on the left..
        selection.minimalWidth = 16;
        selection.mouseOverTopColor = new Color(102, 195, 145, 50);
        selection.activatedTopColor = new Color(188, 118, 114, 50);
        selection.mouseOverBottomColor = new Color(60, 143, 96, 50);
        selection.activatedBottomColor = new Color(151, 79, 79, 50);
        selection.mouseOverPaint = new GradientPaint(0, 0, selection.mouseOverTopColor, 0, 20, selection.mouseOverBottomColor, true);
        selection.activatedPaint = new GradientPaint(0, 0, selection.activatedTopColor, 0, 20, selection.activatedBottomColor, true);


        shadowColor = new Color(35, 35, 35, 105);

        defaultStroke = new BasicStroke(1.0f);
        defaultStrokeColor = Color.black;

        hookLength = 8;

        tmMarginTop = 0;
        tmMarginBottom = 0;

        topChartMargin = 16;
        
        //System.out.println("Generating filters for " + this);
        // filters
        Map<Key, Object> map = new HashMap<>();
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

//        float ninth = 1.0f / 9.0f;
//        float[] blurKernel = {ninth, ninth, ninth, ninth, ninth, ninth, ninth,
//            ninth, ninth};
//        convolutionKernel = new Kernel(3, 3, blurKernel);
//        blurOperator = new ConvolveOp(convolutionKernel, ConvolveOp.EDGE_NO_OP,
//                renderingHints);
    }
}
