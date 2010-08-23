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
package org.gephi.visualization.opengl.text;

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

        String paragraph = text.getLine().text;
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
