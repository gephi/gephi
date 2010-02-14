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

import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.filters.spi.FilterProperty;

/**
 *
 * @author Julian Bilcke <julian.bilcke@gmail.com>
 */
public interface TimelineModel {

    /**
     *
     * @param listener
     */
    public void addListener(TimelineModelListener listener);

    /**
     *
     * @param listener
     */
    public void removeListener(TimelineModelListener listener);

    /**
     * Set the FilterProperty
     */
    public void setFilterProperty(FilterProperty filter);

    /**
     * Get the FilterProperty
     *
     * @return a FilterProperty
     */
    public FilterProperty getFilterProperty();

    /**
     * Get the FilterProperty
     *
     * @return a FilterProperty
     */
    public String getFirstAttributeLabel();

    /**
     *
     * @return
     */
    public String getLastAttributeLabel();

    /**
     *
     * @param i
     * @return
     */
    public String getAttributeLabel(int i);

    /**
     *
     * @param from
     * @param to
     * @return
     */
    public String getAttributeLabel(int from, int to);

    /**
     *
     * @param i
     * @return
     */
    public double getAttributeValue(int i);

    /**
     *
     * @param from
     * @param to
     * @return
     */
    public double getAttributeValue(int from, int to);

    /**
     *
     * @return
     */
    public double getTotalSize();

    /**
     *
     * @return
     */
    public double getRangeSizeValue();

    /**
     *
     * @return
     */
    public double getRangeSizeFloat();

    /**
     *
     * @param min
     */
    public void setMinValue(double min);

    /**
     *
     * @param max
     */
    public void setMaxValue(double max);

    /**
     *
     * @param min
     * @param max
     */
    public void setMinMax(double min, double max);


    /**
     *
     * @return
     */
    public double getMinValue();

    /**
     *
     * @return
     */
    public double getMaxValue();


    /**
     * Set the range using real values
     *
     * @param from
     * @param to
     */
    public void setRangeFromRealValues(double from, double to);

    /**
     *
     * @param from
     * @param to
     */
    public void setRangeFromFloat(double from, double to);

    /**
     *
     * @param from
     */
    public void setFromFloat(double from);

    /**
     *
     * @param to
     */
    public void setToFloat(double to);

    /**
     *
     * @return
     */
    public double getFromFloat();

    /**
     *
     * @return
     */
    public double getToFloat();

    /**
     *
     * @param from
     */
    public void setFromValue(double from);

    /**
     *
     * @param to
     */
    public void setToValue(double to);

    /**
     *
     * @return
     */
    public double getFromValue();

    /**
     *
     * @return
     */
    public double getToValue();

    /**
     * Return the absolute value from a relative value
     *
     * @param position
     * @return
     */
    public double getValueFromFloat(double position);

    /**
     *
     * @return
     */
    public TimeInterval getTimeInterval();

    public void setUnit(Class cl);

    public Class getUnit();
}
