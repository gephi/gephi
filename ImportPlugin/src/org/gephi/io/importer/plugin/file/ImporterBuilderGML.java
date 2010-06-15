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
public class ImporterBuilderGML implements FileImporterBuilder {

    public static final String IDENTIFER = "gml";

    public FileImporter buildImporter() {
        return new ImporterGML();
    }

    public String getName() {
        return IDENTIFER;
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gml", NbBundle.getMessage(getClass(), "fileType_GML_Name"));
        return new FileType[]{ft};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.getExt().equalsIgnoreCase("gml");
    }
}
