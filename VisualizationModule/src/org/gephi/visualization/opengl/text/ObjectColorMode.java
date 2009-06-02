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

import org.gephi.graph.api.Renderable;

/**
 *
 * @author Mathieu Bastian
 */
public class ObjectColorMode implements ColorMode {

    private TextManager manager;

    public ObjectColorMode(TextManager manager) {
        this.manager = manager;
    }

    public void setDefaultColor() {

    }

    public void setTextColor(TextData text) {
        Renderable renderable = text.getObject3d().getObj();
        manager.getRenderer().setColor(renderable.r(), renderable.g(), renderable.b(), renderable.alpha());
    }

}
