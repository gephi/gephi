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
package org.gephi.branding.desktop;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JComponent;
import javax.swing.SingleSelectionModel;
import javax.swing.plaf.ComponentUI;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDisplayerUI;

/**
 *
 * @author Mathieu Bastian
 */
public class NoTabsTabDisplayerUI extends TabDisplayerUI {

    /** Creates a new instance of NoTabsTabDisplayerUI */
    public NoTabsTabDisplayerUI(TabDisplayer displayer) {
        super(displayer);
    }

    public static ComponentUI createUI(JComponent jc) {
        assert jc instanceof TabDisplayer;
        return new NoTabsTabDisplayerUI((TabDisplayer) jc);
    }
    private static final int[] PTS = new int[]{0, 0, 0};

    public Polygon getExactTabIndication(int i) {
        //Should never be called
        return new Polygon(PTS, PTS, PTS.length);
    }

    public Polygon getInsertTabIndication(int i) {
        return new Polygon(PTS, PTS, PTS.length);
    }

    public int tabForCoordinate(Point point) {
        return -1;
    }

    public Rectangle getTabRect(int i, Rectangle rectangle) {
        return new Rectangle(0, 0, 0, 0);
    }

    protected SingleSelectionModel createSelectionModel() {
        return new DefaultSingleSelectionModel();
    }

    public java.lang.String getCommandAtPoint(Point point) {
        return null;
    }

    public int dropIndexOfPoint(Point point) {
        return -1;
    }

    public void registerShortcuts(javax.swing.JComponent jComponent) {
        //do nothing
    }

    public void unregisterShortcuts(javax.swing.JComponent jComponent) {
        //do nothing
    }

    protected void requestAttention(int i) {
        //do nothing
    }

    protected void cancelRequestAttention(int i) {
        //do nothing
    }

    public Dimension getPreferredSize(javax.swing.JComponent c) {
        return new Dimension(0, 0);
    }

    public Dimension getMinimumSize(javax.swing.JComponent c) {
        return new Dimension(0, 0);
    }

    public Dimension getMaximumSize(javax.swing.JComponent c) {
        return new Dimension(0, 0);
    }
}
