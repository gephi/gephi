/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.spi;

/**
 *
 * @author Mathieu Bastian
 */
public interface GraphFileExporterBuilder extends FileExporterBuilder {

    public GraphExporter buildExporter();
}
