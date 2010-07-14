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
	 * Returns the {@code DynamicGraph} of the current workspace or
	 * {@code null} if no graph exists.
	 *
	 * @return the {@code DynamicGraph} of the current workspace or
	 * {@code null} if no graph exists.
	 */
	public DynamicGraph getDynamicGraph();

	/**
	 * Returns the time interval wrapped by the {@code DynamicGraph} of
	 * the current workspace or {@code null} if no graph exists.
	 *
	 * @return the time interval wrapped by the {@code DynamicGraph} of
	 * the current workspace or {@code null} if no graph exists.
	 */
	public TimeInterval getVisibleInterval();

	/**
	 * Sets the time interval wrapped by the {@code DynamicGraph} of
	 * the current workspace.
	 *
	 * @param interval an object to get endpoints from
	 */
	public void setVisibleInterval(TimeInterval interval);

	/**
	 * Sets the time interval wrapped by the {@code DynamicGraph} of
	 * the current workspace.
	 *
	 * @param low  the left endpoint
	 * @param high the right endpoint
	 */
	public void setVisibleInterval(double low, double high);
}
