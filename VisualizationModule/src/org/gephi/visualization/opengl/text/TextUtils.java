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
package org.gephi.visualization.opengl.text;

import org.gephi.visualization.impl.TextDataImpl;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mathieu Bastian
 */
public class TextUtils {

    private TextManager manager;
    private ArrayList<TextDataImpl.TextLine> flowList = new ArrayList<TextDataImpl.TextLine>();

    public TextUtils(TextManager manager) {
        this.manager = manager;
    }

    public void reflow(TextManager.Renderer renderer, int width, TextDataImpl text) {
        flowList.clear();
        int numLines = 0;
        TextRenderer joglRenderer = renderer.getJOGLRenderer();
        FontRenderContext frc = joglRenderer.getFontRenderContext();

        String paragraph = text.getLine().getText();
        Map attrs = new HashMap();
        attrs.put(TextAttribute.FONT, joglRenderer.getFont());
        AttributedString str = new AttributedString(paragraph, attrs);
        LineBreakMeasurer measurer = new LineBreakMeasurer(str.getIterator(), frc);
        int curPos = 0;
        while (measurer.getPosition() < paragraph.length()) {
            int nextPos = measurer.nextOffset(width);
            String line = paragraph.substring(curPos, nextPos);
            TextDataImpl.TextLine textLine = new TextDataImpl.TextLine(line);
            //Rectangle2D bounds = renderer.getBounds(line);
            if (nextPos < paragraph.length()) {
                flowList.add(textLine);
                ++numLines;
            }
            curPos = nextPos;
            measurer.setPosition(curPos);
        }
        if (numLines > 0) {
            text.setWrappedLines(flowList.toArray(new TextDataImpl.TextLine[0]));
        } else {
            text.setWrappedLines(null);
        }
    }
}
