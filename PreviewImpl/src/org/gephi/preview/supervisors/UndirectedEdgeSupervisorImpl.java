package org.gephi.preview.supervisors;

import java.awt.Font;
import java.util.HashSet;
import java.util.Set;
import org.gephi.preview.UndirectedEdgeImpl;
import org.gephi.preview.api.supervisors.UndirectedEdgeSupervisor;
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
        curvedFlag = false;
        colorizer = new EdgeBothBColorMode();
        showLabelsFlag = true;
        shortenLabelsFlag = false;
        labelMaxChar = 10;
        labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        labelColorizer = new ParentColorMode();
    }

    @Override
    protected Set getSupervisedEdges() {
        return supervisedEdges;
    }
}
