/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.importer.api;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Mathieu
 */
public interface ImportController {

    public FileType[] getFileTypes();
    public void doImport(FileObject fileObject) throws ImportException;
}
