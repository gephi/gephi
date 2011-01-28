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
package org.gephi.ui.components.SplineEditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import org.jdesktop.animation.timing.interpolation.Interpolator;
import org.jdesktop.swingx.JXHeader;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Adaptation of the SwingX demo SplineEditor. Used to get a {@link Interpolator} for computing.
 * <p>
 * <a href="http://www.jroller.com/gfx/entry/swing_demos_animations_and_swing">Romain Guy's article<a>
 * 
 * @author Mathieu Bastian
 */
public class SplineEditor extends JDialog {

    private SplineControlPanel splineControlPanel;

    public SplineEditor(String title) throws HeadlessException {
        super(WindowManager.getDefault().getMainWindow(), title, true);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildControlPanel(), BorderLayout.CENTER);

        setResizable(false);

        pack();

        setLocationRelativeTo(getParent());
    }

    private Component buildHeader() {
        ImageIcon icon = new ImageIcon(getClass().getResource("images/simulator.png"));
        JXHeader header = new JXHeader(NbBundle.getMessage(SplineEditor.class, "splineEditor_title"),
                NbBundle.getMessage(SplineEditor.class, "splineEditor_header"),
                icon);
        return header;
    }

    private Component buildControlPanel() {
        splineControlPanel = new SplineControlPanel(this);
        return splineControlPanel;
    }

    public Interpolator getCurrentInterpolator() {
        SplineDisplay display = splineControlPanel.getDisplay();
        Point2D control1 = display.getControl1();
        Point2D control2 = display.getControl2();

        //The TimingFramework implementation doesn't respect the SMIL specification about the returned Y value
		/*Interpolator splines = new SplineInterpolator((float) control1.getX(),
        (float) control1.getY(),
        (float) control2.getX(), (float) control2.getY());*/

        Interpolator splines = new BezierInterpolator((float) control1.getX(),
                (float) control1.getY(),
                (float) control2.getX(), (float) control2.getY());

        return splines;
    }
}
