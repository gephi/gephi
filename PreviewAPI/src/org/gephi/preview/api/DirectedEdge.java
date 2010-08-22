/*
Copyright 2008-2010 Gephi
Authors : Jeremy Subtil <jeremy.subtil@gephi.org>
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
package org.gephi.preview.api;

/**
 * Interface of a preview edge.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface DirectedEdge extends Edge {

    /**
     * Returns an iterable on the arrow list of the edge.
     *
     * @return an iterable on the arrow list of the edge
     */
    public Iterable<EdgeArrow> getArrows();

    /**
     * Returns an iterable on the mini-label list of the edge.
     *
     * @return an iterable on the mini-label list of the edge
     */
    public Iterable<EdgeMiniLabel> getMiniLabels();

    /**
     * Returns whether or not the edge's arrows should be displayed.
     *
     * @return true if the edge's arrows should be displayed
     */
    public Boolean showArrows();

    /**
     * Returns whether or not the edge's mini-labels should be displayed.
     * 
     * @return true if the edge's mini-labels should be displayed
     */
    public Boolean showMiniLabels();
}
