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

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import org.gephi.preview.UndirectedEdgeImpl;
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.api.supervisors.UndirectedEdgeSupervisor;
import org.gephi.preview.propertyeditors.EdgeChildColorizerPropertyEditor;
import org.gephi.preview.propertyeditors.EdgeColorizerPropertyEditor;
import org.gephi.preview.updaters.EdgeBothBColorMode;
import org.gephi.preview.updaters.ParentColorMode;

/**
 * Undirected edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class UndirectedEdgeSupervisorImpl extends EdgeSupervisorImpl
        implements UndirectedEdgeSupervisor {

    protected Set<UndirectedEdgeImpl> supervisedEdges = new HashSet<UndirectedEdgeImpl>();

    /**
     * Constructor.
     *
     * Initializes default values.
     */
    public UndirectedEdgeSupervisorImpl() {
        defaultValues();
    }

    public void defaultValues() {
        curvedFlag = true;
        colorizer = new EdgeBothBColorMode();
        showLabelsFlag = false;
        shortenLabelsFlag = false;
        labelMaxChar = 10;
        baseLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        labelColorizer = new ParentColorMode();
        edgeScale = new Float(1f);
        rescaleWeight = Boolean.TRUE;
    }

    @Override
    protected Set getSupervisedEdges() {
        return supervisedEdges;
    }

    public SupervisorPropery[] getProperties() {
        final String CATEGORY = "Undirected";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "Undirected_curvedFlag", CATEGORY, "Curved", "getCurvedFlag", "setCurvedFlag"),
                        SupervisorPropery.createProperty(this, Float.class, "Undirected_edgeScale", CATEGORY, "Thickness", "getEdgeScale", "setEdgeScale"),
                        SupervisorPropery.createProperty(this, Boolean.class, "Undirected_rescaleWeight", CATEGORY, "Rescale Weight", "getRescaleWeight", "setRescaleWeight"),
                        SupervisorPropery.createProperty(this, EdgeColorizer.class, "Undirected_colorizer", CATEGORY, "Color", "getColorizer", "setColorizer", EdgeColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "Undirected_showLabelsFlag", CATEGORY, "Show labels", "getShowLabelsFlag", "setShowLabelsFlag"),
                        SupervisorPropery.createProperty(this, Boolean.class, "Undirected_shortenLabelsFlag", CATEGORY, "Shorten labels", "getShortenLabelsFlag", "setShortenLabelsFlag"),
                        SupervisorPropery.createProperty(this, Integer.class, "Undirected_labelMaxChar", CATEGORY, "Shorten limit", "getLabelMaxChar", "setLabelMaxChar"),
                        SupervisorPropery.createProperty(this, Font.class, "Undirected_baseLabelFont", CATEGORY, "Font", "getBaseLabelFont", "setBaseLabelFont"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "Undirected_labelColorizer", CATEGORY, "Label color", "getLabelColorizer", "setLabelColorizer", EdgeChildColorizerPropertyEditor.class)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
