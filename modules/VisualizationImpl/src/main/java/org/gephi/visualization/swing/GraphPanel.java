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
package org.gephi.visualization.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.NumberFormat;

import javax.media.opengl.GLJPanel;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizController;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphPanel extends GraphDrawableImpl {

    private GLJPanel gljPanel;
    private NumberFormat formatter;

    public GraphPanel() {
        super();
        formatter = NumberFormat.getNumberInstance();
        formatter.setMaximumFractionDigits(1);

        //Init GLJPanel as the drawable
        gljPanel = new GLJPanel(getCaps(), null, null) {

            @Override
            public void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                render2DBackground(g2d);
                super.paintComponent(g2d);
                render2DForeground(g2d);
            }
        };
        //gljPanel.setOpaque(false);

        graphComponent = gljPanel;
        gljPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        super.initDrawable(gljPanel);

        //Basic panel init
        gljPanel.setLayout(null);
    }

    public GLJPanel getPanel() {
        return gljPanel;
    }

    private void render2DBackground(Graphics2D g) {
    }

    private void render2DForeground(Graphics2D g) {
        if (vizController.getVizConfig().isShowFPS()) {
            g.setColor(Color.LIGHT_GRAY);
            String fpsRound = formatter.format(fps);
            g.drawString(fpsRound, 10, 15);
        }

        GraphLimits limits = VizController.getInstance().getLimits();
        int[] xP = new int[4];
        xP[0] = limits.getMinXviewport();
        xP[1] = limits.getMinXviewport();
        xP[2] = limits.getMaxXviewport();
        xP[3] = limits.getMaxXviewport();
        int[] yP = new int[4];
        yP[0] = viewport.get(3) - limits.getMinYviewport();
        yP[1] = viewport.get(3) - limits.getMaxYviewport();
        yP[2] = viewport.get(3) - limits.getMaxYviewport();
        yP[3] = viewport.get(3) - limits.getMinYviewport();
        g.setColor(Color.red);
        g.drawPolygon(xP, yP, 4);
    }
}
