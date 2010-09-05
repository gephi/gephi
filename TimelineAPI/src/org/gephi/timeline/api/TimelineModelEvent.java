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
package org.gephi.timeline.api;

/**
 *
 * @author Mathieu Bastian
 */
public final class TimelineModelEvent {

    public enum EventType {

        INIT, VISIBLE_INTERVAL, MIN_CHANGED, MAX_CHANGED
    };
    private final EventType type;
    private final TimelineModel source;
    private final Object data;

    public TimelineModelEvent (EventType type, TimelineModel source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
    }

    public EventType getEventType() {
        return type;
    }

    public TimelineModel getSource() {
        return source;
    }

    public Object getData() {
        return data;
    }
}
