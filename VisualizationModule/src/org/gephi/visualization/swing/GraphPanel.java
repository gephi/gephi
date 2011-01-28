/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
