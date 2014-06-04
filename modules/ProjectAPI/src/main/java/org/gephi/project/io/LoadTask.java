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

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.ProjectInformationImpl;
import org.gephi.project.impl.ProjectsImpl;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.gephi.project.spi.WorkspaceBytesPersistenceProvider;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class LoadTask implements LongTask, Runnable {

    private File file;
    private GephiReader gephiReader;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public LoadTask(File file) {
        this.file = file;
    }

    @Override
    public void run() {
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, NbBundle.getMessage(LoadTask.class, "LoadTask.name"));

        try {
            ProjectImpl project = null;
            ZipFile zip = null;
            try {
                zip = new ZipFile(file);

                //Reader
                gephiReader = new GephiReader();

                //Project
                ZipEntry entry = zip.getEntry("Project_xml");
                if (entry != null) {
                    InputStream is = null;
                    try {
                        is = zip.getInputStream(entry);
                        project = readProject(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                }

                //Workspace Xml
                if (project != null) {
                    for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                        entry = e.nextElement();
                        InputStream is = null;
                        String name = entry.getName();
                        if (name.matches("Workspace_[0-9]*_xml")) {
                            try {
                                is = zip.getInputStream(entry);
                                readWorkspace(is, project);
                            } finally {
                                if (is != null) {
                                    is.close();
                                }
                            }
                        }
                    }
                }

                //Other Workspace data
                if (project != null) {
                    for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
                        entry = e.nextElement();
                        InputStream is = null;
                        String name = entry.getName();
                        if (name.matches("Workspace_[0-9]*_.*_bytes")) {
                            try {
                                is = zip.getInputStream(entry);
                                Matcher matcher = Pattern.compile("Workspace_([0-9]*)_(.*)_bytes").matcher(name);
                                matcher.find();
                                String workspaceId = matcher.group(1);
                                String providerId = matcher.group(2);
                                WorkspaceProviderImpl workspaceProvider = project.getLookup().lookup(WorkspaceProviderImpl.class);
                                Workspace workspace = workspaceProvider.getWorkspace(Integer.parseInt(workspaceId));
                                if (workspace != null) {
                                    readWorkspaceBytes(is, workspace, providerId);
                                }
                            } finally {
                                if (is != null) {
                                    is.close();
                                }
                            }
                        }
                    }
                }

                //Add project
                ProjectControllerImpl projectController = Lookup.getDefault().lookup(ProjectControllerImpl.class);
                if (project != null) {
                    if (!cancel) {
                        projectController.openProject(project);
                    }
                }
            } finally {
                if (zip != null) {
                    zip.close();
                }
            }
        } catch (Exception ex) {
            if (ex instanceof GephiFormatException) {
                throw (GephiFormatException) ex;
            }
            throw new GephiFormatException(GephiReader.class, ex);
        }
        Progress.finish(progressTicket);
    }

    private ProjectImpl readProject(InputStream inputStream) throws Exception {
        InputStreamReader isReader = null;
        Xml10FilterReader filterReader = null;
        XMLStreamReader reader = null;
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            }
            inputFactory.setXMLReporter(new XMLReporter() {
                @Override
                public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                    System.out.println("Error:" + errorType + ", message : " + message);
                }
            });
            isReader = new InputStreamReader(inputStream, "UTF-8");
            filterReader = new Xml10FilterReader(isReader);
            reader = inputFactory.createXMLStreamReader(filterReader);

            ProjectControllerImpl projectController = Lookup.getDefault().lookup(ProjectControllerImpl.class);
            ProjectsImpl projects = projectController.getProjects();
            ProjectImpl project = gephiReader.readProject(reader, projects);
            project.getLookup().lookup(ProjectInformationImpl.class).setFile(file);
            return project;
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (filterReader != null) {
                filterReader.close();
            }
        }
    }

    private void readWorkspace(InputStream inputStream, ProjectImpl project) throws Exception {
        InputStreamReader isReader = null;
        Xml10FilterReader filterReader = null;
        XMLStreamReader reader = null;
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            }
            inputFactory.setXMLReporter(new XMLReporter() {
                @Override
                public void report(String message, String errorType, Object relatedInformation, Location location) throws XMLStreamException {
                    System.out.println("Error:" + errorType + ", message : " + message);
                }
            });
            isReader = new InputStreamReader(inputStream, "UTF-8");
            filterReader = new Xml10FilterReader(isReader);
            reader = inputFactory.createXMLStreamReader(filterReader);

            gephiReader.readWorkspace(reader, project);
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (filterReader != null) {
                filterReader.close();
            }
            if (isReader != null) {
                isReader.close();
            }
        }
    }

    private void readWorkspaceBytes(InputStream inputstream, Workspace workspace, String providerId) throws Exception {
        WorkspaceBytesPersistenceProvider provider = PersistenceProviderUtils.getBytesPersistenceProviders().get(providerId);

        if (provider != null) {
            DataInputStream stream = null;
            try {
                stream = new DataInputStream(inputstream);
                provider.readBytes(stream, workspace);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        }
    }

    @Override
    public boolean cancel() {
        cancel = true;
        if (gephiReader != null) {
            gephiReader.cancel();
        }
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
