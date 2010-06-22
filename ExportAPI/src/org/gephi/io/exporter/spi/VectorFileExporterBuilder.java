/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.spi;

/**
 * Exporter builder for vector file format support.
 *
 * @author Mathieu Bastian
 */
public interface VectorFileExporterBuilder extends FileExporterBuilder {

    /**
     * Builds a new vector exporter instance, ready to be used.
     * @return  a new vector exporter
     */
    public VectorExporter buildExporter();
}
