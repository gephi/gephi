/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Blocks user's input when FileChooser is busy.
 *
 * @author Soot Phengsy
 */
public class InputBlocker extends JComponent implements MouseInputListener {
    
    public InputBlocker() {
    }

    private void addListeners(Component c) {
        for( MouseListener ml : c.getMouseListeners() ) {
            if( ml == this )
                return;
        }
        c.addMouseListener(this);
        c.addMouseMotionListener(this);
    }

    private void removeListeners(Component c) {
        c.removeMouseListener(this);
        c.removeMouseMotionListener(this);
    }
    
    public void block(JRootPane rootPane) {
        if( null == rootPane )
            return;
        Component glassPane = rootPane.getGlassPane();
        if( null == glassPane ) {
            rootPane.setGlassPane(this);
            glassPane = this;
        }
        glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        addListeners(glassPane);
        glassPane.setVisible(true);
    }
    
    public void unBlock(JRootPane rootPane) {
        if( null == rootPane )
            return;
        Component glassPane = rootPane.getGlassPane();
        if( null == glassPane ) {
            return;
        }
        removeListeners(glassPane);
        glassPane.setCursor(null);
        glassPane.setVisible(false);
    }

    public void mouseClicked(MouseEvent e) {
        Toolkit.getDefaultToolkit().beep();
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
}
