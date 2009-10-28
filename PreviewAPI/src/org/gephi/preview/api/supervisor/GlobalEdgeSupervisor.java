package org.gephi.preview.api.supervisor;

/**
 * Global edge supervisor.
 *
 * @author Jérémy Subtil <jeremy.subtil@gephi.org>
 */
public interface GlobalEdgeSupervisor {

    /**
	 * Returns true if the edges must be displayed in the preview.
	 *
	 * @return true if the edges must be displayed in the preview
	 */
	public Boolean getShowFlag();

	/**
	 * Defines if the edges must be displayed in the preview.
	 *
	 * @param value  true to display the edges in the preview
	 */
	public void setShowFlag(Boolean value);
}
