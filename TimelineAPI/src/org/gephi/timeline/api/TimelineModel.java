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
package org.gephi.timeline.api;

import org.gephi.filters.spi.FilterProperty;

/**
 *
 * @author Julian Bilcke <julian.bilcke@gmail.com>
 */
public interface TimelineModel {

    public void addListener(TimelineModelListener listener);
    public void removeListener(TimelineModelListener listener);

    public void setFilterProperty(FilterProperty filter);
    public FilterProperty getFilterProperty();

    // for the future chart
    public String getFirstAttributeLabel();
    public String getLastAttributeLabel();
    public String getAttributeLabel(int i);
    public String getAttributeLabel(int from, int to);
    public double getAttributeValue(int i);
    public double getAttributeValue(int from, int to);

    public double getTotalSize();
    public double getRangeSizeValue();
    public double getRangeSizeFloat();

    public void setMinValue(double min);
    public void setMaxValue(double max);
    public void setMinMax(double min, double max);

    public double getMinValue();
    public double getMaxValue();

    // set the range using real values
    public void setRangeFromRealValues(double from, double to);
    public void setRangeFromFloat(double from, double to);

    public void setFromFloat(double from);
    public void setToFloat(double to);
    public double getFromFloat();
    public double getToFloat();

    public void setFromValue(double from);
    public void setToValue(double to);
    public double getFromValue();
    public double getToValue();
}
