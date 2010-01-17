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
        curvedFlag = false;
        colorizer = new EdgeBothBColorMode();
        showLabelsFlag = true;
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
        final String CATEGORY = "Undirected Edge Settings";
        try {
            return new SupervisorPropery[]{
                        SupervisorPropery.createProperty(this, Boolean.class, "curvedFlag", CATEGORY, "Curved Undirected Edges"),
                        SupervisorPropery.createProperty(this, EdgeColorizer.class, "colorizer", CATEGORY, "Undirected Edge Color", EdgeColorizerPropertyEditor.class),
                        SupervisorPropery.createProperty(this, Boolean.class, "showLabelsFlag", CATEGORY, "Show Undirected Edge Labels"),
                        SupervisorPropery.createProperty(this, Boolean.class, "shortenLabelsFlag", CATEGORY, "Shorten Undirected Edge Labels"),
                        SupervisorPropery.createProperty(this, Integer.class, "labelMaxChar", CATEGORY, "Undirected Edge Label Char Limit"),
                        SupervisorPropery.createProperty(this, Font.class, "baseLabelFont", CATEGORY, "Undirected Edge Label Font"),
                        SupervisorPropery.createProperty(this, EdgeChildColorizer.class, "labelColorizer", CATEGORY, "Undirected Edge Label Color", EdgeChildColorizerPropertyEditor.class)};
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new SupervisorPropery[0];
    }
}
