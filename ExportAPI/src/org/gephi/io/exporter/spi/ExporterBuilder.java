/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.spi;

import org.gephi.io.exporter.api.ExportController;

/**
 * Factory class for building exporter instances. Declared in the system as
 * services (i.e. singleton), the role of builders is simply the create new
 * instances of particular exporter on demand.
 * <p>
 * To be recognized by the system, implementations must just add the following annotation:
 * <pre>@ServiceProvider(service=ExporterBuilder.class)</pre>
 *
 * @author Mathieu Bastian
 * @see ExportController
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
