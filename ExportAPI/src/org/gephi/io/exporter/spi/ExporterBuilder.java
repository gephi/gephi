/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.spi;

/**
 *
 * @author Mathieu Bastian
 */
public interface ExporterBuilder {

    /**
     * Builds a new exporter instance, ready to be used.
     * @return  a new exporter
     */
    public Exporter buildExporter();

    /**
     * Returns the name of this builder
     * @return  the name of this exporter
     */
    public String getName();
}
