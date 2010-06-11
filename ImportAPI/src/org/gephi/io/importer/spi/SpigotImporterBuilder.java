/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.spi;

/**
 *
 * @author Mathieu Bastian
 */
public interface SpigotImporterBuilder extends ImporterBuilder {

    public SpigotImporter buildImporter();
}
