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
package org.gephi.visualization.apiimpl;

import java.util.EventObject;

/**
 *
 * @author Mathieu Bastian
 */
public class VizEvent extends EventObject {

    public enum Type {

        START_DRAG,
        DRAG,
        STOP_DRAG,
        MOUSE_MOVE,
        MOUSE_LEFT_PRESS,
        MOUSE_MIDDLE_PRESS,
        MOUSE_RIGHT_PRESS,
        MOUSE_LEFT_CLICK,
        MOUSE_MIDDLE_CLICK,
        MOUSE_RIGHT_CLICK,
        NODE_LEFT_CLICK,
        MOUSE_LEFT_PRESSING,
        MOUSE_RELEASED,
        NODE_LEFT_PRESS,
        NODE_LEFT_PRESSING,
    };
    private Type type;
    private Object data;

    public VizEvent(Object source, Type t) {
        super(source);
        this.type = t;
    }

    public VizEvent(Object source, Type t, Object data) {
        super(source);
        this.type = t;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
