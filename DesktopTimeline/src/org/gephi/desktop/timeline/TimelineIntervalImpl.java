/*
Copyright 2010 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
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
package org.gephi.desktop.timeline;


import org.gephi.timeline.api.TimelineInterval;
import org.gephi.timeline.api.TimelineIntervalListener;
import org.openide.util.lookup.ServiceProvider;

/**
 * Timeline Data Provider Service Implementation
 * Provide the timeline data service to generate charts
 * @author Julian Bilcke <julian.bilcke@gmail.com>
 */
@ServiceProvider(service = TimelineInterval.class)
public class TimelineIntervalImpl implements TimelineInterval {

    public int getLength() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getFirstAttributeLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getLastAttributeLabel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAttributeLabel(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getAttributeLabel(int from, int to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getAttributeValue(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getAttributeValue(int from, int to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public TimelineInterval getSubInterval(int from, int to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addListener(TimelineIntervalListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeListener(TimelineIntervalListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
