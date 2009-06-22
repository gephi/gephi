/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.statistics.api;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import org.gephi.graph.api.GraphController;

/**
 *
 * @author Mathieu Bastian, Patrick J. McSweeney
 */
public interface Statistics {

    /**
     *
     * @return The name of the Staistics
     */
    public String getName();

    /**
     * Executes the Staistics
     * @param graphController
     * @param progressMonitor
     */
    public void execute(GraphController graphController,
            ProgressMonitor progressMonitor);

    /**
     * (Possibly the same as (getPanel() == null)
     * @return True if the Statistics is parameterizable via a JPanel
     */
    public boolean isParamerizable();

    /**
     * 
     * @return A JPanel which ties into the objects member variables.
     */
    public JPanel getPanel();

    /**
     *
     * @return An HTML string that displays the results for this Statistics
     */
    public String getReport();
}
