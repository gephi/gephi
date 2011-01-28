/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
