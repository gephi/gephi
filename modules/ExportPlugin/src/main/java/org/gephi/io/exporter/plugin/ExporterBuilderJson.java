package org.gephi.io.exporter.plugin;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = GraphFileExporterBuilder.class)
public class ExporterBuilderJson implements GraphFileExporterBuilder {

    @Override
    public GraphExporter buildExporter() {
        return new ExporterJson();
    }

    @Override
    public FileType[] getFileTypes() {
        FileType ft = new FileType(".json", NbBundle.getMessage(ExporterBuilderJson.class, "fileType_Json_Name"));
        return new FileType[] {ft};
    }

    @Override
    public String getName() {
        return "Json";
    }
}
