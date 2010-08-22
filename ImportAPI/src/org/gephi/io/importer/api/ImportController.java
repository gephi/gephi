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
package org.gephi.io.importer.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.Workspace;

/**
 * Manage and control the import executionf low.
 * <p>
 * This controller is a singleton and can therefore be found in Lookup:
 * <pre>ImportController ic = Lookup.getDefault().lookup(ImportController.class);</pre>
 * @author Mathieu Bastian
 */
public interface ImportController {

    public Container importFile(File file) throws FileNotFoundException;

    public Container importFile(File file, FileImporter importer) throws FileNotFoundException;

    public Container importFile(Reader reader, FileImporter importer);

    public Container importFile(InputStream stream, FileImporter importer);

    public Container importSpigot(SpigotImporter importer);

    public FileImporter getFileImporter(File file);

    public FileImporter getFileImporter(String importerName);

    public Container importDatabase(Database database, DatabaseImporter importer);

    public void process(Container container);

    public void process(Container container, Processor processor, Workspace workspace);

    public FileType[] getFileTypes();

    public boolean isFileSupported(File file);

    public ImporterUI getUI(Importer importer);
}
