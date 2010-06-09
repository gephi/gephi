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
public final class ImporterBuilderCSV implements FileImporterBuilder {

    public static final String IDENTIFER = "csv";

    public FileImporter getImporter() {
        return new ImporterCSV();
    }

    public String getIdentifier() {
        return IDENTIFER;
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".csv", NbBundle.getMessage(getClass(), "fileType_CSV_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.getExt().equalsIgnoreCase("csv");
    }
}
