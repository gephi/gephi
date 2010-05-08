/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.importer.api;

import java.io.InputStream;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.DatabaseType;
import org.gephi.io.importer.spi.FileFormatImporter;
import org.gephi.io.processor.spi.Processor;
import org.openide.filesystems.FileObject;

/**
 * Manage and control the import executionf low.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>ImportController ic = Lookup.getDefault().lookup(ImportController.class);</pre>
 * @author Mathieu Bastian
 */
public interface ImportController {

    public FileFormatImporter getFileImporter(FileObject fileObject);

    public FileFormatImporter getFileImporter(String importerName);

    public DatabaseImporter getDatabaseImporter(Database database);

    public Container importFile(InputStream stream, FileFormatImporter importer);

    public Container importDatabase(Database database, DatabaseImporter importer);

    public void process(Container container);

    public void process(Container container, Processor processor);

    public FileType[] getFileTypes();

    public DatabaseType[] getDatabaseTypes();

    public Database[] getDatabases(DatabaseType type);

    public DatabaseType getDatabaseType(Database database);

    public boolean isFileSupported(FileObject fileObject);
}
