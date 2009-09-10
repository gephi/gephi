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

import java.io.BufferedWriter;
import org.gephi.io.exporter.GraphFileExporter;
import org.gephi.io.exporter.FileType;
import org.gephi.io.exporter.TextExporter;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ExporterGDF implements GraphFileExporter, TextExporter, LongTask {

    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public boolean exportData(BufferedWriter writer) throws Exception {

        Progress.start(progressTicket);

        writer.write("test");

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
        return NbBundle.getMessage(getClass(), "ExporterGDF_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gdf", NbBundle.getMessage(getClass(), "fileType_GDF_Name"));
        return new FileType[]{ft};
    }
}
