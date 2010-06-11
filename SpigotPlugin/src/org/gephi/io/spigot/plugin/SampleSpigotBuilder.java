/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.spigot.plugin;

import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.io.importer.spi.SpigotImporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = SpigotImporterBuilder.class)
public class SampleSpigotBuilder implements SpigotImporterBuilder {

    public SpigotImporter buildImporter() {
        return new SampleSpigot();
    }

    public String getName() {
        return "Sample";
    }
}
