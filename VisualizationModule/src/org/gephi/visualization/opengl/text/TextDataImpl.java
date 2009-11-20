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

import java.awt.Rectangle;
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
    float a;
    float size = 1f;
    float sizeFactor = 1f;
    boolean visible;

    public TextLine getLine() {
        return line;
    }

    public void setWrappedLines(TextDataImpl.TextLine[] lines) {
        this.wrappedLines = lines;
    }

    public void setLine(String line) {
        this.line = new TextLine(line);
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

    public float getWidth() {
        return (float) line.bounds.getWidth() * sizeFactor;
    }

    public float getHeight() {
        return (float) line.bounds.getHeight() * sizeFactor;
    }

    public String getText() {
        return line.text;
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

        String text = "rien";
        Rectangle2D bounds = new Rectangle(20, 20);

        public TextLine() {
        }

        public TextLine(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setBounds(Rectangle2D bounds) {
            this.bounds = bounds;
        }
    }
}
