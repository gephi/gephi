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
