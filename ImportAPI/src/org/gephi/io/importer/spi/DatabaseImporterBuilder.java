/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.spi;

/**
 * Importer builder specific for {@link DatabaseImporter}.
 * 
 * @author Mathieu Bastian
 */
public interface DatabaseImporterBuilder extends ImporterBuilder {

    /**
     * Builds a new database importer instance, ready to be used.
     * @return  a new database importer
     */
    public DatabaseImporter buildImporter();
}
