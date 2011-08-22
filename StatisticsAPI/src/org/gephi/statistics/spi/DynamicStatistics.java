/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 *           Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
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
package org.gephi.statistics.spi;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.Interval;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;

/**
 *
 * @author Mathieu Bastian
 */
public interface DynamicStatistics extends Statistics {

    public void execute(GraphModel graphModel, AttributeModel model);

    public void loop(GraphView window, Interval interval);

    public void end();

    public void setBounds(Interval bounds);

    public void setWindow(double window);

    public void setTick(double tick);

    public double getWindow();

    public double getTick();

    public Interval getBounds();
}
