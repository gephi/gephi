/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.preview;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.io.exporter.spi.VectorFileExporterBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = VectorFileExporterBuilder.class)
public class ExporterBuilderPDF implements VectorFileExporterBuilder {

    public VectorExporter buildExporter() {
        return new PDFExporter();
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".pdf", NbBundle.getMessage(ExporterBuilderPDF.class, "fileType_PDF_Name"));
        return new FileType[]{ft};
    }

    public String getName() {
        return NbBundle.getMessage(ExporterBuilderPDF.class, "ExporterPDF_name");
    }
}
