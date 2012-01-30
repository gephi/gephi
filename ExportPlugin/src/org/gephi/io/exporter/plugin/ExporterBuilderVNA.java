/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.io.exporter.plugin;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = GraphFileExporterBuilder.class)
public class ExporterBuilderVNA implements GraphFileExporterBuilder{
    public GraphExporter buildExporter() {
        return new ExporterVNA();
    }

    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".vna", "Netdraw VNA")};
    }

    public String getName() {
        return "vna";
    }
    
}
