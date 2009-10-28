package org.gephi.preview.supervisor;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.gephi.preview.SelfLoopImpl;
import org.gephi.preview.api.color.colorizer.EdgeColorizer;
import org.gephi.preview.api.supervisor.SelfLoopSupervisor;
import org.gephi.preview.color.colormode.CustomColorMode;

/**
 * Self-loop supervisor implementation.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public class SelfLoopSupervisorImpl implements SelfLoopSupervisor {

	private Boolean showSelfLoops = true;
    private EdgeColorizer selfLoopColorizer = new CustomColorMode(0, 0, 0);
	private final Set<SelfLoopImpl> supervisedSelfLoops = Collections.newSetFromMap(new WeakHashMap<SelfLoopImpl, Boolean>());

	/**
	 * Adds the given self-loop to the list of the supervised self-loops.
	 * 
	 * It updates the self-loop with the supervisor's values.
	 * 
	 * @param selfLoop  the self-loop to supervise
	 */
	public void addSelfLoop(SelfLoopImpl selfLoop) {
        supervisedSelfLoops.add(selfLoop);

        colorSelfLoops();
    }

	/**
	 * Returns true if the self-loops must be displayed in the preview.
	 *
	 * @return true if the self-loops must be displayed in the preview
	 */
	public boolean getShowSelfLoops() {
		return showSelfLoops;
	}

	/**
	 * Defines if the self-loops must be displayed in the preview.
	 *
	 * @param value  true to display the self-loops in the preview
	 */
	public void setShowSelfLoops(Boolean value) {
		showSelfLoops = value;
	}

	/**
	 * Returns the self-loop colorizer.
	 *
	 * @return the self-loop colorizer
	 */
	public EdgeColorizer getSelfLoopColorizer() {
		return selfLoopColorizer;
	}

	/**
	 * Sets the self-loop colorizer.
	 *
	 * @param value  the self-loop colorizer to set
	 */
	public void setSelfLoopColorizer(EdgeColorizer value) {
		selfLoopColorizer = value;
		colorSelfLoops();
	}

	/**
	 * Colors the given self-loop.
	 * 
	 * @param selfLoop  the self-loop to color
	 */
	private void colorSelfLoop(SelfLoopImpl selfLoop) {
        selfLoopColorizer.color(selfLoop);
    }

	/**
	 * Colors the supervised self-loops.
	 */
    private void colorSelfLoops() {
        for (SelfLoopImpl sl : supervisedSelfLoops) {
            colorSelfLoop(sl);
        }
    }
}
