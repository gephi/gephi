/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.io.exporter.spi;

import org.gephi.project.api.Workspace;

/**
 * Define a class that can export data. Different sub-interfaces are available
 * for byte and character targets.
 *
 * @author Mathieu Bastian
 */
public interface Exporter {

    /**
     * Run the export process.
     * @return          <code>true</code> if the operation is successful,
     *                  <code>false</code> if it has been cancelled
     */
    public boolean execute();

    /**
     * Sets the worksapce from where to export data
     * @param workspace the workspace to export
     */
    public void setWorkspace(Workspace workspace);

    /**
     * Returns the workspace from where data are exported
     * @return          the workspace the data are to be exported
     */
    public Workspace getWorkspace();
}
