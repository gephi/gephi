/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.dynamic.api;

/**
 *
 * @author Mathieu Bastian
 */
public final class DynamicModelEvent {

    public enum EventType {

        VISIBLE_INTERVAL, IS_DYNAMIC
    };
    private final EventType type;
    private final DynamicModel source;
    private final Object data;

    public DynamicModelEvent(EventType type, DynamicModel source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }

    public EventType getEventType() {
        return type;
    }

    public DynamicModel getSource() {
        return source;
    }

    public Object getData() {
        return data;
    }
}
