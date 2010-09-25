/*
Copyright 2008-2010 Gephi
Authors : Julian Bilcke <julian.bilcke@gephi.org>
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

import org.gephi.dynamic.api.DynamicModel;

/**
 *
 * @author Julian Bilcke <julian.bilcke@gmail.com>
 */
public interface TimelineModel {

    public void setup(DynamicModel dynamicModel);

    public void unsetup();

    public void disable();

    public double getTotalSize();

    public double getMinValue();

    public double getMaxValue();

    public double getFromFloat();

    public double getToFloat();

    public void setCustomMin(double min);

    public void setCustomMax(double max);

    public void setRangeFromFloat(double from, double to);

    public double getValueFromFloat(double position);

    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    public void setUnit(Class cl);

    public Class getUnit();
}
