/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.importer.plugin.file;

import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FileImporterBuilder.class)
public final class ImporterBuilderDL implements FileImporterBuilder {

    public static final String IDENTIFER = "dl";

    public FileImporter getImporter() {
        return new ImporterDL();
    }

    public String getIdentifier() {
        return IDENTIFER;
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".dl", NbBundle.getMessage(getClass(), "fileType_DL_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.getExt().equalsIgnoreCase("dl");
    }
}
