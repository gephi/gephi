package org.gephi.preview.api.supervisor;

import org.gephi.preview.api.color.colorizer.EdgeColorizer;

/**
 * Self-loop supervisor.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface SelfLoopSupervisor {

	/**
	 * Returns true if the self-loops must be displayed in the preview.
	 *
	 * @return true if the self-loops must be displayed in the preview
	 */
	public Boolean getShowFlag();

	/**
	 * Defines if the self-loops must be displayed in the preview.
	 *
	 * @param value  true to display the self-loops in the preview
	 */
	public void setShowFlag(Boolean value);

	/**
	 * Returns the self-loop colorizer.
	 *
	 * @return the self-loop colorizer
	 */
	public EdgeColorizer getColorizer();

	/**
	 * Sets the self-loop colorizer.
	 *
	 * @param value  the self-loop colorizer to set
	 */
	public void setColorizer(EdgeColorizer value);
}
