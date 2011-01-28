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
package org.gephi.preview;

import java.awt.Font;
import org.gephi.preview.api.Color;
import org.gephi.preview.api.EdgeColorizerClient;
import org.gephi.preview.api.EdgeMiniLabel;
import org.gephi.preview.api.supervisors.DirectedEdgeSupervisor;
import org.gephi.preview.util.HAlignImpl;
import org.gephi.preview.api.util.Holder;

/**
 * Implementation of an edge mini-label.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public abstract class EdgeMiniLabelImpl extends AbstractEdgeLabel
        implements EdgeMiniLabel {

    protected final DirectedEdgeImpl parent;
    protected HAlignImpl hAlign;

    /**
     * Constructor.
     *
     * @param parent  the parent edge of the edge label
     * @param value   the value of the edge label
     */
    public EdgeMiniLabelImpl(DirectedEdgeImpl parent, String value) {
        super(value);
        this.parent = parent;
    }

    /**
     * Returns the directed edge supervisor.
     *
     * @return the directed edge supervisor
     */
    public DirectedEdgeSupervisor getDirectedEdgeSupervisor() {
        return ((DirectedEdgeImpl) parent).getDirectedEdgeSupervisor();
    }

    /**
     * Generates the edge mini-label's position.
     */
    public abstract void genPosition();

    public HAlignImpl getHAlign() {
        return hAlign;
    }

    public Font getFont() {
        return parent.getMiniLabelFont();
    }

    public Float getAngle() {
        return parent.getAngle();
    }

    public EdgeColorizerClient getParentEdge() {
        return parent;
    }

    public Holder<Color> getParentColorHolder() {
        return parent.getColorHolder();
    }
}
