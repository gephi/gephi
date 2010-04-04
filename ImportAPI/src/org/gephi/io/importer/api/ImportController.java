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
import org.gephi.io.importer.spi.DatabaseType;
import org.openide.filesystems.FileObject;

/**
 * Manage and control the import executionf low.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>ImportController ic = Lookup.getDefault().lookup(ImportController.class);</pre>
 * @author Mathieu Bastian
 */
public interface ImportController {

    public FileType[] getFileTypes();

    public void doImport(FileObject fileObject);

    public void doImport(InputStream stream, String importer);

    public void doImport(Database database);

    public DatabaseType[] getDatabaseTypes();

    public Database[] getDatabases(DatabaseType type);

    public boolean isFileSupported(FileObject fileObject);
}
