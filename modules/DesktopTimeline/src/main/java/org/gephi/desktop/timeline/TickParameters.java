/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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

import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Mathieu Bastian
 */
public class TickParameters {

    public enum TickType {

        DATE, DOUBLE, START_END
    }

    private final TickType type;
    private int width, height;
    private int fontSize = 12;
    private double fontFactor = 6.;
    private Font font = new Font("Helvetica", Font.PLAIN, fontSize);
        private Color[] realColors = new Color[]{new Color(0xB4B4B4), new Color(0x5A5A5A), new Color(0x1E1E1E)};
    private Color[] dateColors = new Color[]{new Color(0xB4B4B4), new Color(0x5A5A5A)};

    public TickParameters(TickType type) {
        this.type = type;
    }

    public TickType getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Font getFont() {
        return font;
    }

    public Color getDateColor(int level) {
        return dateColors[level];
    }

    public void setDateColor(int level, Color color) {
        dateColors[level] = color;
    }

    public Color getRealColor(int level) {
        return realColors[level];
    }

    public void setLevelColor(int level, Color color) {
        realColors[level] = color;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public double getFontFactor() {
        return fontFactor;
    }

    public void setFontFactor(double fontFactor) {
        this.fontFactor = fontFactor;
    }
}
