/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter;

import org.gephi.workspace.api.Workspace;

/**
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

    public boolean isExportVisible() {
        return exportVisible;
    }

    public Workspace getWorkspace() {
        return workspace;
    }
}
