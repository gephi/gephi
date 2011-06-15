/*
Copyright 2008-2011 Gephi
Authors : Daniel Bernardes <daniel.bernardes@polytechnique.edu>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.exporter.plugin;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Daniel Bernardes
 */
@ServiceProvider(service = GraphFileExporterBuilder.class)
public class ExporterBuilderPajek implements GraphFileExporterBuilder {

    public GraphExporter buildExporter() {
        return new ExporterPajek();
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".net", NbBundle.getMessage(ExporterBuilderCSV.class, "fileType_Pajek_Name"));
        return new FileType[]{ft};
    }

    public String getName() {
        return "Pajek";
    }
}
