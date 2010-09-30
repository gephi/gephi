/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
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
 * Event from the dynamic model.
 * <ul>
 * <li><b>VISIBLE_INTERVAL:</b> The visible interval set by the timeline has changed</li>
 * <li><b>MIN_CHANGED:</b> The minimum bound in time has changed</li>
 * <li><b>MAX_CHANGED:</b> The maximum bound in time has changed</li>
 * </ul>
 * @author Cezary Bartosiak
 * @author Mathieu Bastian
 */
public final class DynamicModelEvent {

    /**
     * Event from the dynamic model.
     * <p>
     * The visible interval is a <code>TimeInterval</code> object. For min and
     * max changed, data are <code>Double</code> objects.
     * <ul>
     * <li><b>VISIBLE_INTERVAL:</b> The visible interval set by the timeline has changed</li>
     * <li><b>MIN_CHANGED:</b> The minimum bound in time has changed</li>
     * <li><b>MAX_CHANGED:</b> The maximum bound in time has changed</li>
     * </ul>
     */
    public enum EventType {

        VISIBLE_INTERVAL, MIN_CHANGED, MAX_CHANGED
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
