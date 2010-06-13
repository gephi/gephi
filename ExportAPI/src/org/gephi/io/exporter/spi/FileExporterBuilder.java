/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.spi;

import org.gephi.io.exporter.api.FileType;

/**
 *
 * @author Mathieu Bastian
 */
public interface FileExporterBuilder extends ExporterBuilder {

    public Exporter buildExporter();

    /**
     * Get default file types this exporter can deal with.
     * @return  an array of file types this exporter can read
     */
    public FileType[] getFileTypes();
}
