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
import org.gephi.preview.api.EdgeChildColorizer;
import org.gephi.preview.api.EdgeColorizer;
import org.gephi.preview.api.SupervisorPropery;
import org.gephi.preview.propertyeditors.EdgeChildColorizerPropertyEditor;
import org.gephi.preview.propertyeditors.EdgeColorizerPropertyEditor;
import org.gephi.preview.updaters.EdgeBothBColorMode;
import org.gephi.preview.updaters.ParentColorMode;

/**
 * Bidirectional edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class BidirectionalEdgeSupervisorImpl extends DirectedEdgeSupervisorImpl {

    /**
     * Constructor.
     *
     * Initializes default values.
     */
    public BidirectionalEdgeSupervisorImpl() {
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
        showMiniLabelsFlag = false;
        shortenMiniLabelsFlag = false;
        miniLabelMaxChar = 10;
        miniLabelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 8);
        miniLabelAddedRadius = 15f;
        miniLabelColorizer = new ParentColorMode();
        showArrowsFlag = true;
        arrowAddedRadius = 65f;
        arrowSize = 20f;
        arrowColorizer = new ParentColorMode();
        edgeScale = new Float(1f);
    }

    @Override
    public SupervisorPropery[] getProperties() {
        final String CATEGORY = "Bidirectional";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "Bidirectional_curvedFlag", CATEGORY, "Curved", "getCurvedFlag", "setCurvedFlag"),
                        SupervisorPropery.createProperty(this, Float.class, "Bidirectional_edgeScale", CATEGORY, "Thickness", "getEdgeScale", "setEdgeScale"),
                        SupervisorPropery.createProperty(this, EdgeColorizer.class, "Bidirectional_colorizer", CATEGORY, "Color", "getColorizer", "setColorizer", EdgeColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "Bidirectional_showLabelsFlag", CATEGORY, "Labels", "getShowLabelsFlag", "setShowLabelsFlag"),
                        SupervisorPropery.createProperty(this, Boolean.class, "Bidirectional_shortenLabelsFlag", CATEGORY, "Shorten labels", "getShortenLabelsFlag", "setShortenLabelsFlag"),
                        SupervisorPropery.createProperty(this, Integer.class, "Bidirectional_labelMaxChar", CATEGORY, "Shorten limit", "getLabelMaxChar", "setLabelMaxChar"),
                        SupervisorPropery.createProperty(this, Font.class, "Bidirectional_baseLabelFont", CATEGORY, "Font", "getBaseLabelFont", "setBaseLabelFont"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "Bidirectional_labelColorizer", CATEGORY, "Label color", "getLabelColorizer", "setLabelColorizer", EdgeChildColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "Bidirectional_showMiniLabelsFlag", CATEGORY, "Mini-Labels", "getShowMiniLabelsFlag", "setShowMiniLabelsFlag"),
                        SupervisorPropery.createProperty(this, Float.class, "Bidirectional_miniLabelAddedRadius", CATEGORY, "Mini-Label radius", "getMiniLabelAddedRadius", "setMiniLabelAddedRadius"),
                        SupervisorPropery.createProperty(this, Boolean.class, "Bidirectional_shortenMiniLabelsFlag", CATEGORY, "Shorten Mini-Labels", "getShortenMiniLabelsFlag", "setShortenMiniLabelsFlag"),
                        SupervisorPropery.createProperty(this, Integer.class, "Bidirectional_miniLabelMaxChar", CATEGORY, "Mini-Label limit", "getMiniLabelMaxChar", "setMiniLabelMaxChar"),
                        SupervisorPropery.createProperty(this, Font.class, "Bidirectional_miniLabelFont", CATEGORY, "Mini-Label font", "getMiniLabelFont", "setMiniLabelFont"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "Bidirectional_miniLabelColorizer", CATEGORY, "Mini-Label color", "getMiniLabelColorizer", "setMiniLabelColorizer", EdgeChildColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "Bidirectional_showArrowsFlag", CATEGORY, "Arrows", "getShowArrowsFlag", "setShowArrowsFlag"),
                        SupervisorPropery.createProperty(this, Float.class, "Bidirectional_arrowAddedRadius", CATEGORY, "Arrow added radius", "getArrowAddedRadius", "setArrowAddedRadius"),
                        SupervisorPropery.createProperty(this, Float.class, "Bidirectional_arrowSize", CATEGORY, "Arrow size", "getArrowSize", "setArrowSize"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "Bidirectional_arrowColorizer", CATEGORY, "Arrow color", "getArrowColorizer", "setArrowColorizer", EdgeChildColorizerPropertyEditor.class)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
