/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.plugin;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = GraphFileExporterBuilder.class)
public class ExporterBuilderGraphML implements GraphFileExporterBuilder {

    public GraphExporter buildExporter() {
        return new ExporterGraphML();
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".graphml", NbBundle.getMessage(ExporterBuilderGraphML.class, "fileType_GraphML_Name"));
        return new FileType[]{ft};
    }

    public String getName() {
        return NbBundle.getMessage(ExporterBuilderGEXF.class, "ExporterGraphML_name");
    }
}
