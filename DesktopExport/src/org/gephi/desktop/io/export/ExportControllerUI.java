/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.io.export;

import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.spi.Exporter;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Mathieu Bastian
 */
public interface ExportControllerUI {

    public void exportFile(final FileObject fileObject, final Exporter exporter);

    public ExportController getExportController();
}
