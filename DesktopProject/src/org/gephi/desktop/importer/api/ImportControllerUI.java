/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.desktop.importer.api;

import java.io.InputStream;
import java.io.Reader;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.SpigotImporter;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Mathieu Bastian
 */
public interface ImportControllerUI {

    public void importFile(FileObject fileObject);

    public void importStream(InputStream stream, String importerName);

    public void importFile(Reader reader, String importerName);

    public void importDatabase(Database database, DatabaseImporter importer);

    public void importDatabase(DatabaseImporter importer);

    public void importSpigot(SpigotImporter importer);

    public ImportController getImportController();
}
