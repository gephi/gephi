/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.spi;

/**
 * Importer builder specific for {@link SpigotImporter}.
 *
 * @author Mathieu Bastian
 */
public interface SpigotImporterBuilder extends ImporterBuilder {

    /**
     * Builds a new spigot importer instance, ready to be used.
     * @return  a new spigot importer
     */
    public SpigotImporter buildImporter();
}
