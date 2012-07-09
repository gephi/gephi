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
package org.gephi.visualization.impl;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import org.gephi.graph.api.TextData;

/**
 *
 * @author Mathieu Bastian
 */
public class TextDataImpl implements TextData {

    TextLine line = new TextLine();
    TextLine[] wrappedLines;
    float r = -1;
    float g;
    float b;
    float a = 1f;
    float size = 1f;
    float sizeFactor = 1f;
    boolean visible = true;

    public TextLine getLine() {
        return line;
    }

    public void setWrappedLines(TextDataImpl.TextLine[] lines) {
        this.wrappedLines = lines;
    }

    public void setText(String line) {
        this.line = new TextLine(line, this.line.bounds);
    }

    public boolean hasCustomColor() {
        return r > 0;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setSizeFactor(float sizeFactor) {
        this.sizeFactor = sizeFactor * size;
    }

    public void setColor(float r, float g, float b, float alpha) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = alpha;
    }

    public void setColor(Color color) {
        if (color == null) {
            r = -1;
        } else {
            setColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        }
    }

    public float getWidth() {
        Rectangle2D rec = line.bounds;
        if (rec != null) {
            return (float) rec.getWidth() * sizeFactor;
        }
        return 0f;
    }

    public float getHeight() {
        Rectangle2D rec = line.bounds;
        if (rec != null) {
            return (float) rec.getHeight() * sizeFactor;
        }
        return 0f;
    }

    public String getText() {
        return line.text;
    }

    public float getSizeFactor() {
        return sizeFactor;
    }

    public float getSize() {
        return size;
    }

    public float getR() {
        return r;
    }

    public float getG() {
        return g;
    }

    public float getB() {
        return b;
    }

    public float getAlpha() {
        return a;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public static class TextLine {

        String text = "";
        Rectangle2D bounds;

        public TextLine() {
        }

        public TextLine(String text) {
            this.text = text;
        }

        public TextLine(String text, Rectangle2D bounds) {
            this.text = text;
            this.bounds = bounds;
        }

        public String getText() {
            return text;
        }

        public void setBounds(Rectangle2D bounds) {
            this.bounds = bounds;
        }
    }
}
