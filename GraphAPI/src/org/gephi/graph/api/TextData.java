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
package org.gephi.graph.api;

import java.awt.Color;

/**
 * Contains all extended data related to text display.
 *
 * @author Mathieu Bastian
 */
public interface TextData {

    public float getWidth();

    public float getHeight();

    public String getText();

    public float getSize();

    public float getR();

    public float getG();

    public float getB();

    public float getAlpha();

    public boolean isVisible();

    public void setColor(float r, float g, float b, float alpha);

    public void setColor(Color color);

    public void setSize(float size);

    public void setVisible(boolean visible);
}
