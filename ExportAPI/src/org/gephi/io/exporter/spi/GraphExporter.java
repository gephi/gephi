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

/**
 * Exporter interface for exporters that export the graph, either complete or
 * filtered (i.e. visible graph).
 * 
 * @author Mathieu Bastian
 */
public interface GraphExporter extends Exporter {

    /**
     * Sets if only the visible graph has to be exported. If <code>false</code>,
     * the complete graph is exported.
     * @param exportVisible the export visible parameter value
     */
    public void setExportVisible(boolean exportVisible);

    /**
     * Returns <code>true</code> if only the visible graph has to be exported.
     * @return  <code>true</code> if only the visible graph has to be exported,
     *          <code>false</code> for the complete graph.
     */
    public boolean isExportVisible();
}
