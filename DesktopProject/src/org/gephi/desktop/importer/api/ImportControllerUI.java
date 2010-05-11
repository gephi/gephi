/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.desktop.importer.api;

import java.io.InputStream;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.api.ImportController;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Mathieu Bastian
 */
public interface ImportControllerUI {

    public void importFile(FileObject fileObject);

    public void importStream(InputStream stream, String importer);

    public void importDatabase(Database database);

    public ImportController getImportController();
}
