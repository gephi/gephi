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
    }

    @Override
    protected Set getSupervisedEdges() {
        return supervisedEdges;
    }

    public SupervisorPropery[] getProperties() {
        final String CATEGORY = "Undirected";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "curvedFlag", CATEGORY, "Curved"),
                        SupervisorPropery.createProperty(this, EdgeColorizer.class, "colorizer", CATEGORY, "Color", EdgeColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "showLabelsFlag", CATEGORY, "Show labels"),
                        SupervisorPropery.createProperty(this, Boolean.class, "shortenLabelsFlag", CATEGORY, "Shorten labels"),
                        SupervisorPropery.createProperty(this, Integer.class, "labelMaxChar", CATEGORY, "Shorten limit"),
                        SupervisorPropery.createProperty(this, Font.class, "baseLabelFont", CATEGORY, "Font"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "labelColorizer", CATEGORY, "Label color", EdgeChildColorizerPropertyEditor.class)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
