/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.io.importer.spi;

import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.Report;

/**
 * Interface for classes which imports data from files, databases, streams or other sources.
 *
 * @author Mathieu Bastian
 */
public interface Importer {

    public boolean execute();

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

    /**
     * Sets the import container. All data imported are pushed to this container.
     * It is the import "result".
     * @param container the import container where data are to be pushed
     */
    public void setContainer(ContainerLoader container);

    /**
     * Sets the import report. Informations, logs and issues are pushed to
     * <code>report</code> during import for further analysis and verification.
     * @param report    the import report object
     */
    public void setReport(Report report);
}
