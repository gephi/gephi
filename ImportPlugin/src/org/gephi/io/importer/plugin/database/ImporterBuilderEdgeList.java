/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.plugin.database;

import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.DatabaseImporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = DatabaseImporterBuilder.class)
public class ImporterBuilderEdgeList implements DatabaseImporterBuilder {

    public static final String IDENTIFER = "edgelist";

    public DatabaseImporter buildImporter() {
        return new ImporterEdgeList();
    }

    public String getName() {
        return IDENTIFER;
    }
}
