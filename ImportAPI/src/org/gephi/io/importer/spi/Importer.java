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
package org.gephi.io.importer.spi;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.api.Report;

/**
 * Interface for classes which imports data from files, databases, streams or other sources.
 * <p>
 * Importers are built from {@link ImporterBuilder} services and can be configured
 * by {@link ImporterUI} classes.
 *
 * @author Mathieu Bastian
 * @see ImportController
 */
public interface Importer {

    /**
     * Run the import processus.
     * @param loader    the container where imported data will be pushed
     * @return          <code>true</code> if the import is successfull or
     *                  <code>false</code> if it has been cancelled
     */
    public boolean execute(ContainerLoader loader);

    /**
     * Returns the import container. The container is the import "result", all
     * data found during import are being pushed to the container.
     * @return          the import container
     */
    public ContainerLoader getContainer();

    /**
     * Returns the import report, filled with logs and potential issues.
     * @return          the import report
     */
    public Report getReport();
}
