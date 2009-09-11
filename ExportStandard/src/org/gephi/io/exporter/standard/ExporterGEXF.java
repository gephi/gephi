/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.exporter.standard;

import org.gephi.graph.api.Graph;
import org.gephi.io.exporter.GraphFileExporter;
import org.gephi.io.exporter.FileType;
import org.gephi.io.exporter.XMLExporter;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ExporterGEXF implements GraphFileExporter, XMLExporter, LongTask {

    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public boolean exportData(Document document, Graph graph) throws Exception {
        Progress.start(progressTicket);

        Element root = document.createElementNS("http://www.gephi.org/gexf", "gexf");
        document.appendChild(root);

        Progress.finish(progressTicket);
        return !cancel;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "ExporterGEXF_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gexf", NbBundle.getMessage(getClass(), "fileType_GEXF_Name"));
        return new FileType[]{ft};
    }
}
