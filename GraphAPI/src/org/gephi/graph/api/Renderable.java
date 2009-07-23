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


/**
 *
 * @author Mathieu Bastian
 */
public interface Renderable extends Spatial {

    public void setX(float x);

    public void setY(float y);

    public void setZ(float z);

    public float getRadius();

    public float getSize();

    public void setSize(float size);

    public float r();

    public float g();

    public float b();

    public void setR(float r);

    public void setG(float g);

    public void setB(float b);

    public float alpha();

    public void setAlpha(float alpha);

    public Model getModel();

    public void setModel(Model obj);

    public TextData getTextData();

    public void setTextData(TextData textData);

    public void setLabelVisible(boolean value);

    public boolean isLabelVisible();
}
