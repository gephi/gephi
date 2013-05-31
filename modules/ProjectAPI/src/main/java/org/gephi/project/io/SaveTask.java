/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.project.io;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Project;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.gephi.project.spi.WorkspaceBytesPersistenceProvider;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Mathieu Bastian
 */
public class SaveTask implements LongTask, Runnable {

    private static final String ZIP_LEVEL_PREFERENCE = "ProjectIO_Save_ZipLevel_0_TO_9";
    private File file;
    private Project project;
    private GephiWriter gephiWriter;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public SaveTask(Project project, File file) {
        this.project = project;
        this.file = file;
    }

    @Override
    public void run() {
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, NbBundle.getMessage(SaveTask.class, "SaveTask.name"));

        File writeFile = null;
        try {
            String tempFileName = file.getName() + "_temp" + System.currentTimeMillis();
            writeFile = new File(file.getParent(), tempFileName);

            FileOutputStream outputStream = null;
            ZipOutputStream zipOut = null;
            BufferedOutputStream bos = null;
            DataOutputStream dos = null;
            try {
                //Stream
                int zipLevel = NbPreferences.forModule(SaveTask.class).getInt(ZIP_LEVEL_PREFERENCE, 9);
                outputStream = new FileOutputStream(writeFile);
                zipOut = new ZipOutputStream(outputStream);
                zipOut.setLevel(zipLevel);
                bos = new BufferedOutputStream(zipOut);
                dos = new DataOutputStream(bos);

                //Writer
                gephiWriter = new GephiWriter();

                //Write Project
                writeProject(gephiWriter, bos, zipOut);

                //Write Workspace files
                for (Workspace ws : project.getLookup().lookup(WorkspaceProviderImpl.class).getWorkspaces()) {
                    writeWorkspace(ws, gephiWriter, dos, zipOut);
                    writeWorkspaceBytes(ws, dos, zipOut);
                    if (cancel) {
                        break;
                    }
                }

                zipOut.finish();
            } finally {
                if (dos != null) {
                    try {
                        dos.close();
                    } catch (IOException ex1) {
                    }
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException ex1) {
                    }
                }
                if (zipOut != null) {
                    try {
                        zipOut.close();
                    } catch (IOException ex1) {
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException ex1) {
                    }
                }
            }

            //Rename file
            if (!cancel && writeFile.exists()) {
                FileObject fileObject = FileUtil.toFileObject(file);
                String name = fileObject.getName();
                String ext = fileObject.getExt();

                //Delete original file
                fileObject.delete();

                //Rename
                FileObject tempFileObject = FileUtil.toFileObject(writeFile);
                FileLock lock = tempFileObject.lock();
                tempFileObject.rename(lock, name, ext);
                lock.releaseLock();
            }

        } catch (Exception ex) {
            if (ex instanceof GephiFormatException) {
                throw (GephiFormatException) ex;
            }
            throw new GephiFormatException(SaveTask.class, ex);
        } finally {
            if (writeFile != null && writeFile.exists()) {
                FileObject tempFileObject = FileUtil.toFileObject(writeFile);
                try {
                    tempFileObject.delete();
                } catch (IOException ex) {
                }
            }
        }

        Progress.finish(progressTicket);
    }

    private void writeProject(GephiWriter gephiWriter, OutputStream outputStream, ZipOutputStream zipOut) throws Exception {
        XMLStreamWriter writer = null;
        //Write Project file
        zipOut.putNextEntry(new ZipEntry("Project_xml"));
        try {
            //Create Writer and write project
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);
            writer = outputFactory.createXMLStreamWriter(outputStream, "UTF-8");
            gephiWriter.writeProject(writer, project);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        //Close Project file
        zipOut.closeEntry();
    }

    private void writeWorkspace(Workspace workspace, GephiWriter gephiWriter, OutputStream outputStream, ZipOutputStream zipOut) throws Exception {
        //Write Project file
        zipOut.putNextEntry(new ZipEntry("Workspace_" + workspace.getId() + "_xml"));

        XMLStreamWriter writer = null;
        try {
            //Create Writer and write project
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);
            writer = outputFactory.createXMLStreamWriter(outputStream, "UTF-8");
            gephiWriter.writeWorkspace(writer, workspace);

        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        //Close Project file
        zipOut.closeEntry();
    }

    private void writeWorkspaceBytes(Workspace workspace, DataOutputStream outputStream, ZipOutputStream zipOut) throws Exception {
        for (Map.Entry<String, WorkspaceBytesPersistenceProvider> entry : PersistenceProviderUtils.getBytesPersistenceProviders().entrySet()) {
            String name = entry.getKey();
            WorkspaceBytesPersistenceProvider provider = entry.getValue();

            //Write Project file
            zipOut.putNextEntry(new ZipEntry("Workspace_" + workspace.getId() + "_" + name + "_bytes"));

            provider.writeBytes(outputStream, workspace);

            //Close Project file
            zipOut.closeEntry();
        }
    }

    @Override
    public boolean cancel() {
        if (gephiWriter != null) {
            gephiWriter.cancel();
        }
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
