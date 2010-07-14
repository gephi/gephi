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
package org.gephi.dynamic;

import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicGraph;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Workspace;

/**
 * The default implementation of {@code DynamicModel}.
 *
 * @author Cezary Bartosiak
 */
public final class DynamicModelImpl implements DynamicModel {
	private Workspace    workspace;
	private DynamicGraph dynamicGraph;
	private double       low;
	private double       high;

	/**
	 * The default constructor.
	 *
	 * @param workspace  workspace related to this model
	 *
	 * @throws NullPointerException if {@code workspace} is null.
	 */
	public DynamicModelImpl(Workspace workspace) {
		this(workspace, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Constructs a new {@code DynamicModel} for the {@code workspace}.
	 *
	 * @param workspace  workspace related to this model
	 * @param low        the left endpoint of the visible time interval
	 * @param high       the right endpoint of the visible time interval
	 *
	 * @throws NullPointerException if {@code workspace} is null.
	 */
	public DynamicModelImpl(Workspace workspace, double low, double high) {
		if (workspace == null)
			throw new NullPointerException("The workspace cannot be null.");

		this.workspace = workspace;
		this.low       = low;
		this.high      = high;

		GraphModel gm = (GraphModel)workspace.getLookup().lookup(GraphModel.class);
		if (gm != null && gm.getGraph() != null)
			dynamicGraph = new DynamicGraphImpl(gm.getGraph(), low, high);
	}

	@Override
	public DynamicGraph getDynamicGraph() {
		if (dynamicGraph == null) {
			GraphModel gm = (GraphModel)workspace.getLookup().lookup(GraphModel.class);
			if (gm != null && gm.getGraph() != null)
				dynamicGraph = new DynamicGraphImpl(gm.getGraph(), low, high);
		}
		return dynamicGraph;
	}

	@Override
	public TimeInterval getVisibleInterval() {
		if (dynamicGraph != null)
			return dynamicGraph.getInterval();
		return null;
	}

	@Override
	public void setVisibleInterval(TimeInterval interval) {
		setVisibleInterval(interval.getLow(), interval.getHigh());
	}

	@Override
	public void setVisibleInterval(double low, double high) {
		this.low  = low;
		this.high = high;
		if (dynamicGraph != null)
			dynamicGraph.setInterval(low, high);
	}
}
