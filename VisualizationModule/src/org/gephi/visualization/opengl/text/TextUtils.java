/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.visualization.opengl.text;

import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Mathieu
 */
public class TextUtils {

    private TextManager manager;
    private ArrayList<TextData.TextLine> flowList = new ArrayList<TextData.TextLine>();

    public TextUtils(TextManager manager) {
    }

    public void reflow(int width, TextData text) {
        flowList.clear();
        int numLines = 0;
        TextRenderer renderer = manager.getRenderer();
        FontRenderContext frc = renderer.getFontRenderContext();

        String paragraph = text.getLine().text;
        Map attrs = new HashMap();
        attrs.put(TextAttribute.FONT, renderer.getFont());
        AttributedString str = new AttributedString(paragraph, attrs);
        LineBreakMeasurer measurer = new LineBreakMeasurer(str.getIterator(), frc);
        int curPos = 0;
        while (measurer.getPosition() < paragraph.length()) {
            int nextPos = measurer.nextOffset(width);
            String line = paragraph.substring(curPos, nextPos);
            TextData.TextLine textLine = new TextData.TextLine(line);
            //Rectangle2D bounds = renderer.getBounds(line);
            if (nextPos < paragraph.length()) {
                flowList.add(textLine);
                ++numLines;
            }
            curPos = nextPos;
            measurer.setPosition(curPos);
        }
        if (numLines > 0) {
            text.setWrappedLines(flowList.toArray(new TextData.TextLine[0]));
        } else {
            text.setWrappedLines(null);
        }
    }
}
