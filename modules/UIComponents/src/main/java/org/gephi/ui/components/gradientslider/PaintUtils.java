/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.components.gradientslider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.SwingConstants;
import javax.swing.UIManager;


// Author Jeremy Wood
public class PaintUtils {

    /** Four shades of white, each with increasing opacity. */
    public final static Color[] whites = new Color[]{
        new Color(255, 255, 255, 50),
        new Color(255, 255, 255, 100),
        new Color(255, 255, 255, 150)
    };
    /** Four shades of black, each with increasing opacity. */
    public final static Color[] blacks = new Color[]{
        new Color(0, 0, 0, 50),
        new Color(0, 0, 0, 100),
        new Color(0, 0, 0, 150)
    };

    /** @return the color used to indicate when a component has
     * focus.  By default this uses the color (64,113,167), but you can
     * override this by calling:
     * <BR><code>UIManager.put("focusRing",customColor);</code>
     */
    public static Color getFocusRingColor() {
        Object obj = UIManager.getColor("focusRing");
        if (obj instanceof Color) {
            return (Color) obj;
        }
        return new Color(64, 113, 167);
    }

    /** Paints 3 different strokes around a shape to indicate focus.
     * The widest stroke is the most transparent, so this achieves a nice
     * "glow" effect.
     * <P>The catch is that you have to render this underneath the shape,
     * and the shape should be filled completely.
     *
     * @param g the graphics to paint to
     * @param shape the shape to outline
     * @param biggestStroke the widest stroke to use.
     */
    public static void paintFocus(Graphics2D g, Shape shape, int biggestStroke) {
        Color focusColor = getFocusRingColor();
        Color[] focusArray = new Color[]{
            new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), 255),
            new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), 170),
            new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), 110)
        };
        g.setStroke(new BasicStroke(biggestStroke));
        g.setColor(focusArray[2]);
        g.draw(shape);
        g.setStroke(new BasicStroke(biggestStroke - 1));
        g.setColor(focusArray[1]);
        g.draw(shape);
        g.setStroke(new BasicStroke(biggestStroke - 2));
        g.setColor(focusArray[0]);
        g.draw(shape);
        g.setStroke(new BasicStroke(1));
    }

    /** Uses translucent shades of white and black to draw highlights
     * and shadows around a rectangle, and then frames the rectangle
     * with a shade of gray (120).
     * <P>This should be called to add a finishing touch on top of
     * existing graphics.
     * @param g the graphics to paint to.
     * @param r the rectangle to paint.
     */
    public static void drawBevel(Graphics g, Rectangle r) {
        drawColors(blacks, g, r.x, r.y + r.height, r.x + r.width, r.y + r.height, SwingConstants.SOUTH);
        drawColors(blacks, g, r.x + r.width, r.y, r.x + r.width, r.y + r.height, SwingConstants.EAST);

        drawColors(whites, g, r.x, r.y, r.x + r.width, r.y, SwingConstants.NORTH);
        drawColors(whites, g, r.x, r.y, r.x, r.y + r.height, SwingConstants.WEST);

        g.setColor(new Color(120, 120, 120));
        g.drawRect(r.x, r.y, r.width, r.height);
    }

    private static void drawColors(Color[] colors, Graphics g, int x1, int y1, int x2, int y2, int direction) {
        for (int a = 0; a < colors.length; a++) {
            g.setColor(colors[colors.length - a - 1]);
            if (direction == SwingConstants.SOUTH) {
                g.drawLine(x1, y1 - a, x2, y2 - a);
            } else if (direction == SwingConstants.NORTH) {
                g.drawLine(x1, y1 + a, x2, y2 + a);
            } else if (direction == SwingConstants.EAST) {
                g.drawLine(x1 - a, y1, x2 - a, y2);
            } else if (direction == SwingConstants.WEST) {
                g.drawLine(x1 + a, y1, x2 + a, y2);
            }
        }
    }
}
