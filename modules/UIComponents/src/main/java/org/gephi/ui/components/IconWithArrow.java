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
package org.gephi.ui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;

//author S. Aubrecht from org.openide.awt
public class IconWithArrow implements Icon {

    private static final String ARROW_IMAGE_NAME = "org/openide/awt/resources/arrow.png"; //NOI18N
    private final Icon orig;
    private final Icon arrow = ImageUtilities.image2Icon(ImageUtilities.loadImage(ARROW_IMAGE_NAME, false));
    private final boolean paintRollOver;
    private static final int GAP = 6;

    public IconWithArrow(Icon orig, boolean paintRollOver) {
        this.orig = orig;
        this.paintRollOver = paintRollOver;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        int height = getIconHeight();
        orig.paintIcon(c, g, x, y + (height - orig.getIconHeight()) / 2);

        arrow.paintIcon(c, g, x + GAP + orig.getIconWidth(), y + (height - arrow.getIconHeight()) / 2);

        if (paintRollOver) {
            Color brighter = UIManager.getColor("controlHighlight"); //NOI18N
            Color darker = UIManager.getColor("controlShadow"); //NOI18N
            if (null == brighter || null == darker) {
                brighter = c.getBackground().brighter();
                darker = c.getBackground().darker();
            }
            if (null != brighter && null != darker) {
                g.setColor(brighter);
                g.drawLine(x + orig.getIconWidth() + 1, y,
                        x + orig.getIconWidth() + 1, y + getIconHeight());
                g.setColor(darker);
                g.drawLine(x + orig.getIconWidth() + 2, y,
                        x + orig.getIconWidth() + 2, y + getIconHeight());
            }
        }
    }

    @Override
    public int getIconWidth() {
        return orig.getIconWidth() + GAP + arrow.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return Math.max(orig.getIconHeight(), arrow.getIconHeight());
    }

    public static int getArrowAreaWidth() {
        return GAP / 2 + 5;
    }
}
