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
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Project;
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

    public void run() {
        //System.out.println("Save " + dataObject.getName());
        ZipOutputStream zipOut = null;
        boolean useTempFile = false;
        File writeFile = null;
        try {
            Progress.start(progressTicket);
            Progress.setDisplayName(progressTicket, NbBundle.getMessage(SaveTask.class, "SaveTask.name"));
            FileObject fileObject = FileUtil.toFileObject(file);
            writeFile = file;
            if (writeFile.exists()) {
                useTempFile = true;
                String tempFileName = writeFile.getName() + "_temp";
                writeFile = new File(writeFile.getParent(), tempFileName);
            }

            //Stream
            int zipLevel = NbPreferences.forModule(SaveTask.class).getInt(ZIP_LEVEL_PREFERENCE, 9);
            FileOutputStream outputStream = new FileOutputStream(writeFile);
            zipOut = new ZipOutputStream(outputStream);
            zipOut.setLevel(zipLevel);

            zipOut.putNextEntry(new ZipEntry("Project"));
            gephiWriter = new GephiWriter();

            //Create Writer and write project
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.FALSE);
            BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(zipOut);
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter(bufferedOutputStream, "UTF-8");
            gephiWriter.writeAll(project, writer);
            writer.close();

            //Close
            zipOut.closeEntry();
            zipOut.finish();
            bufferedOutputStream.close();



            //Clean and copy
            if (useTempFile && !cancel) {
                String name = fileObject.getName();
                String ext = fileObject.getExt();

                //Delete original file
                fileObject.delete();

                //Rename temp file
                FileObject tempFileObject = FileUtil.toFileObject(writeFile);
                FileLock lock = tempFileObject.lock();
                tempFileObject.rename(lock, name, ext);
                lock.releaseLock();
            } else if (cancel) {
                //Delete temp file
                FileObject tempFileObject = FileUtil.toFileObject(writeFile);
                tempFileObject.delete();
            }
            Progress.finish(progressTicket);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (zipOut != null) {
                try {
                    zipOut.close();
                } catch (IOException ex1) {
                }
            }
            if (useTempFile && writeFile != null) {
                writeFile.delete();
            }
            throw new GephiFormatException(GephiWriter.class, ex);
        }
    }

    public boolean cancel() {
        if (gephiWriter != null) {
            gephiWriter.cancel();
        }
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
