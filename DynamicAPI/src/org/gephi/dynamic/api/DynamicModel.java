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

import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Graph;

/**
 * Root interface that contains the complete dynamic graph structure and build
 * {@link DynamicGraph} objets on demand.
 * 
 * @author Cezary Bartosiak
 *
 * @see DynamicController
 */
public interface DynamicModel {
	/**
	 * The name of a column containing time intervals.
	 */
	public static final String TIMEINTERVAL_COLUMN = "time_interval";

	/**
	 * Builds a new {@code DynamicGraph} from the given {@code Graph} instance.
	 *
	 * @param graph the underlying graph
	 *
	 * @return a new a new {@code DynamicGraph}.
	 */
	public DynamicGraph createDynamicGraph(Graph graph);

	/**
	 * Builds a new {@code DynamicGraph} from the given {@code Graph} instance
	 * wrapping the given {@code TimeInterval}.
	 *
	 * @param graph the underlying graph
	 *
	 * @return a new a new {@code DynamicGraph}.
	 */
	public DynamicGraph createDynamicGraph(Graph graph, TimeInterval interval);

	/**
	 * Returns the time interval wrapped by the {@code DynamicGraph} of
	 * the current workspace.
	 *
	 * @return the time interval wrapped by the {@code DynamicGraph} of
	 * the current workspace.
	 */
	public TimeInterval getVisibleInterval();
}
