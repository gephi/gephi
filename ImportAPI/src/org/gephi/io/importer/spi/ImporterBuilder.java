/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.spi;

import org.gephi.io.importer.api.ImportController;

/**
 * Factory class for building importer instances. Declared in the system as
 * services (i.e. singleton), the role of builders is simply the create new
 * instances of particular importer on demand.
 * <p>
 * To be recognized by the system, implementations must just add the following annotation:
 * <pre>@ServiceProvider(service=ImporterBuilder.class)</pre>
 *
 * @author Mathieu Bastian
 * @see ImportController
 */
public interface ImporterBuilder {

    /**
     * Builds a new importer instance, ready to be used.
     * @return  a new importer
     */
    public Importer buildImporter();

    /**
     * Returns the name of this builder
     * @return  the name of this importer
     */
    public String getName();
}
