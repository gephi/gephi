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
package org.gephi.io.container;

import org.gephi.io.importer.Importer;
import org.gephi.io.logging.Report;
import org.gephi.io.processor.Processor;

/**
 * A container is created each time data are imported by <b>importers</b>. Its role is to host all data
 * collected by importers during import process. After pushing data in the container, its content can be
 * analysed to verify its validity and then be processed by <b>processors</b>. Thus containers are
 * <b>loaded</b> by importers and <b>unloaded</b> by processors.
 * <p>
 * See {@link ContainerLoader} for how to push graph and attributes data in the container and see
 * {@link  ContainerUnloader} for how to retrieve data in the container.
 *
 * @author Mathieu Bastian
 * @see Importer
 * @see Processor
 */
public interface Container {

    /**
     * Set the source of the data put in the container. Could be a file name.
     * @param source the original source of data.
     * @throws NullPointerException if <code>source</code> is <code>null</code>
     */
    public void setSource(String source);

    /**
     * If exists, returns the source of the data.
     * @return the source of the data, or <code>null</code> if source is not defined.
     */
    public String getSource();

    /**
     * Get containers loading interface. The <b>loader</b> is used by modules which put data in the
     * container, whereas the <b>unloader</b> interface is used by modules which read containers content.
     * @return the containers loading interface
     */
    public ContainerLoader getLoader();

    /**
     * Get containers unloading interface. The <b>unloader</b> interface is used by modules which read
     * containers content, whereas the <b>loader</b> is used for pushing data in the container.
     * @return the container unloading interface
     */
    public ContainerUnloader getUnloader();

    public void setAutoScale(boolean autoscale);

    public boolean isAutoScale();

    public void setAllowSelfLoop(boolean value);

    public void setAllowAutoNode(boolean value);

    public void setAllowParallelEdge(boolean value);

    /**
     * Set a report this container can use to report issues detected when loading the container. Report
     * are used to log info and issues during import process. Only one report can be associated to a
     * container.
     * @param report set <code>report</code> as the default report for this container
     * @throws NullPointerException if <code>report</code> is <code>null</code>
     */
    public void setReport(Report report);

    /**
     * Returns the report associated to this container, if exists.
     * @return the report set for this container or <code>null</code> if no report is defined
     */
    public Report getReport();

    /**
     * This method must be called after the loading is complete and before unloading. Its aim is to verify data consistency as a whole.
     * @return <code>true</code> if container data is consistent, <code>false</code> otherwise
     */
    public boolean verify();

    /**
     * Close the current loading and clean content before unloading.
     */
    public void closeLoader();

    public boolean isDynamicGraph();

    public boolean isHierarchicalGraph();
}
