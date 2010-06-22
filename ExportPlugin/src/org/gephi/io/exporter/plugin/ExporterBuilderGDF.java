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
public class ExporterBuilderGDF implements GraphFileExporterBuilder {

    public GraphExporter buildExporter() {
        return new ExporterGDF();
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gdf", NbBundle.getMessage(ExporterBuilderGDF.class, "fileType_GDF_Name"));
        return new FileType[]{ft};
    }

    public String getName() {
        return NbBundle.getMessage(ExporterBuilderGDF.class, "ExporterGDF_name");
    }
}
