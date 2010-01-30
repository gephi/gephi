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
package org.gephi.io.exporter.spi;

import org.gephi.workspace.api.Workspace;

/**
 * General settings class that is used by {@link GraphFileExporter} to get
 * the export context.
 * 
 * @author Mathieu Bastian
 */
public final class GraphFileExporterSettings {

    private Workspace workspace;
    private boolean exportVisible;

    public GraphFileExporterSettings(Workspace workspace, boolean exportVisible) {
        this.workspace = workspace;
        this.exportVisible = exportVisible;
    }

    /**
     * Returns <code>true</code> if only the visible graph has to be exported.
     * @return  <code>true</code> if only the visible graph has to be exported,
     *          <code>false</code> for the complete graph.
     */
    public boolean isExportVisible() {
        return exportVisible;
    }

    /**
     * The workspace the <code>GraphModel</code> has to be retrieved.
     *
     * @return  the workspace the graph is belonging
     */
    public Workspace getWorkspace() {
        return workspace;
    }
}
