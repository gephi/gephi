/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.spi;

import org.gephi.io.importer.api.FileType;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Mathieu Bastian
 */
public interface FileImporterBuilder extends ImporterBuilder {

    public FileImporter getImporter();

    /**
     * Get default file types this importer can deal with.
     * @return an array of file types this importer can read
     */
    public FileType[] getFileTypes();

    /**
     * Returns <code>true</code> if this importer can import <code>fileObject</code>. Called from
     * controllers to identify dynamically which importers can be used for a particular file format.
     * <p>
     * Use <code>FileObject.getExt()</code> to retrieve file extension. Matching can be done not only with
     * metadata but also with file content. The <code>fileObject</code> can be read in that way.
     * @param fileObject the file in input
     * @return <code>true</code> if the importer is compatible with <code>fileObject</code> or <code>false</code>
     * otherwise
     */
    public boolean isMatchingImporter(FileObject fileObject);
}
