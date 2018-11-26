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
package org.gephi.ui.components.splineeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.geom.Point2D;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import org.jdesktop.swingx.JXHeader;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * Adaptation of the SwingX demo SplineEditor. Used to get a Interpolator for computing.
 * <a href="http://www.jroller.com/gfx/entry/swing_demos_animations_and_swing">Romain Guy's article</a>
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

    public Point2D getControl1() {
        SplineDisplay display = splineControlPanel.getDisplay();
        return display.getControl1();
    }

    public Point2D getControl2() {
        SplineDisplay display = splineControlPanel.getDisplay();
        return display.getControl2();
    }

    public void setControl1(Point2D control1) {
        SplineDisplay display = splineControlPanel.getDisplay();
        display.setControl1(control1);
    }

    public void setControl2(Point2D control2) {
        SplineDisplay display = splineControlPanel.getDisplay();
        display.setControl2(control2);
    }
//    public Interpolator getCurrentInterpolator() {
//        SplineDisplay display = splineControlPanel.getDisplay();
//        Point2D control1 = display.getControl1();
//        Point2D control2 = display.getControl2();
//
//        //The TimingFramework implementation doesn't respect the SMIL specification about the returned Y value
//		/*Interpolator splines = new SplineInterpolator((float) control1.getX(),
//        (float) control1.getY(),
//        (float) control2.getX(), (float) control2.getY());*/
//
//        Interpolator splines = new BezierInterpolator((float) control1.getX(),
//                (float) control1.getY(),
//                (float) control2.getX(), (float) control2.getY());
//
//        return splines;
//    }
}
