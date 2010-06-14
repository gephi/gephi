/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.spi;

/**
 * Exporter builder for graph file format support.
 *
 * @author Mathieu Bastian
 */
public interface GraphFileExporterBuilder extends FileExporterBuilder {

    /**
     * Builds a new graph exporter instance, ready to be used.
     * @return  a new graph exporter
     */
    public GraphExporter buildExporter();
}
