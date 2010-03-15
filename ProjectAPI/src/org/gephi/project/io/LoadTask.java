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
package org.gephi.project.io;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.ProjectInformationImpl;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;

/**
 *
 * @author Mathieu Bastian
 */
public class LoadTask implements LongTask, Runnable {

    private GephiDataObject dataObject;
    private GephiReader gephiReader;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public LoadTask(GephiDataObject dataObject) {
        this.dataObject = dataObject;
    }

    public void run() {
        try {
            Progress.start(progressTicket);
            Progress.setDisplayName(progressTicket, NbBundle.getMessage(SaveTask.class, "LoadTask.name"));
            FileObject fileObject = dataObject.getPrimaryFile();
            if (FileUtil.isArchiveFile(fileObject)) {
                //Unzip
                fileObject = FileUtil.getArchiveRoot(fileObject).getChildren()[0];
            }

            //Parse documcent
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(fileObject.getInputStream());

            //Project instance
            Project project = dataObject.getProject();
            if (project == null) {
                project = new ProjectImpl();
            }
            project.getLookup().lookup(ProjectInformationImpl.class).setDataObject(dataObject);
            dataObject.setProject(project);

            //Version
            String version = doc.getDocumentElement().getAttribute("version");
            if (version == null || version.isEmpty() || Double.parseDouble(version) != 0.7) {
                throw new GephiFormatException("Gephi project file version must be at least 0.7");
            }

            //GephiReader
            gephiReader = new GephiReader();
            project = gephiReader.readAll(doc.getDocumentElement(), project);

            //Add project
            if (!cancel) {
                ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
                pc.openProject(project);
            }
            Progress.finish(progressTicket);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex instanceof GephiFormatException) {
                throw (GephiFormatException) ex;
            }
            throw new GephiFormatException(GephiReader.class, ex);
        }
    }

    public boolean cancel() {
        cancel = true;
        gephiReader.cancel();
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
