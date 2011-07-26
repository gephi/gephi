package org.gephi.io.exporter.ucinet;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = GraphFileExporterBuilder.class)
public class ExporterBuilderDL implements GraphFileExporterBuilder 
{

    @Override
    public GraphExporter buildExporter() {
       return new ExporterDL();
    }

    @Override
    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".dl", "dl files(UCINET)")};
    }

    @Override
    public String getName() {
        return "dl";
    }
    
}
