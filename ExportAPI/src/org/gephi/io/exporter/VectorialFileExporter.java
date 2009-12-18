/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter;

import java.io.File;
import org.gephi.workspace.api.Workspace;

/**
 *
 * @author Mathieu Bastian
 */
public interface VectorialFileExporter extends FileExporter {

    public boolean exportData(File file, Workspace workspace);
}
