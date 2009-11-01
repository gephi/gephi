package org.gephi.preview.supervisor;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.gephi.preview.EdgeImpl;
import org.gephi.preview.api.supervisor.GlobalEdgeSupervisor;

/**
 * Global edge supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class GlobalEdgeSupervisorImpl implements GlobalEdgeSupervisor {

    private boolean showEdges = true;
    private final Set<EdgeImpl> supervisedEdges = Collections.newSetFromMap(new WeakHashMap<EdgeImpl, Boolean>());

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
}
