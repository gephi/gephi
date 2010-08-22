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
package org.gephi.preview.supervisors;

import java.util.HashSet;
import java.util.Set;
import org.gephi.preview.EdgeImpl;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.GlobalEdgeSupervisor;

/**
 * Global edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class GlobalEdgeSupervisorImpl implements GlobalEdgeSupervisor {

    //Properties
    private boolean showEdges;
    //Architecture
    private final Set<EdgeImpl> supervisedEdges = new HashSet<EdgeImpl>();

    public GlobalEdgeSupervisorImpl() {
        defaultValues();
    }

    public void defaultValues() {
        showEdges = true;
    }

    /**
     * Adds the given edge to the list of the supervised edges.
     *
     * It updates the edge with the supervisor's values.
     *
     * @param edge  the edge to supervise
     */
    public void addEdge(EdgeImpl edge) {
        supervisedEdges.add(edge);
    }

    /**
     * Clears the list of supervised edges.
     */
    public void clearSupervised() {
        supervisedEdges.clear();
    }

    /**
     * Returns true if the edges must be displayed in the preview.
     *
     * @return true if the edges must be displayed in the preview
     */
    public Boolean getShowFlag() {
        return showEdges;
    }

    /**
     * Defines if the edges must be displayed in the preview.
     *
     * @param value  true to display the edges in the preview
     */
    public void setShowFlag(Boolean value) {
        showEdges = value;
    }

    public SupervisorPropery[] getProperties() {
        final String CATEGORY = "Edge";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "GlobalEdge_showFlag", CATEGORY, "Show", "getShowFlag", "setShowFlag")};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
