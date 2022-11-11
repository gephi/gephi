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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.gephi.project.api.GephiFormatException;
import org.gephi.project.api.LegacyGephiFormatException;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.ProjectInformationImpl;
import org.gephi.project.impl.ProjectsImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.project.spi.WorkspaceBytesPersistenceProvider;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 * @author Mathieu Bastian
 */
public class LoadTask implements LongTask {

    private final File file;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public LoadTask(File file) {
        this.file = file;
    }

    public static XMLStreamReader newXMLReader(InputStream is) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
            inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
        }
        inputFactory.setXMLReporter(new XMLReporter() {
            @Override
            public void report(String message, String errorType, Object relatedInformation,
                               Location location) throws XMLStreamException {
            }
        });
        InputStreamReader isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Xml10FilterReader filterReader = new Xml10FilterReader(isReader);
        return inputFactory.createXMLStreamReader(filterReader);
    }

    public ProjectImpl execute(ProjectsImpl projects) {
        Progress.start(progressTicket);
        Progress.setDisplayName(progressTicket, NbBundle.getMessage(LoadTask.class, "LoadTask.name"));

        try {
            ZipFile zip = null;
            try {
                if (!file.exists()) {
                    throw new FileNotFoundException("File " + file.getPath() + " not found");
                }
                zip = new ZipFile(file);

                ProjectImpl project = readProject(zip, projects);

                if (project != null) {
                    // Enumerate workspaces
                    List<String> workspaceEntries = new ArrayList<>();
                    for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements(); ) {
                        ZipEntry entry = e.nextElement();
                        if (entry.getName().matches("Workspace_[0-9]*_xml")) {
                            workspaceEntries.add(entry.getName());
                        }
                    }

                    // Get providers
                    Collection<WorkspacePersistenceProvider> providers =
                        PersistenceProviderUtils.getPersistenceProviders();

                    //Setup progress
                    Progress.switchToDeterminate(progressTicket, (1 + providers.size()) * workspaceEntries.size());

                    // Read workspaces
                    for (String workspaceEntry : workspaceEntries) {
                        WorkspaceImpl workspace = readWorkspace(project, workspaceEntry, zip);

                        Progress.progress(progressTicket);

                        if (workspace != null) {
                            for (WorkspacePersistenceProvider provider : providers) {
                                if (provider instanceof WorkspaceXMLPersistenceProvider) {
                                    try {
                                        readWorkspaceChildrenXML((WorkspaceXMLPersistenceProvider) provider, workspace,
                                            zip);
                                    } catch (Exception e) {
                                        Logger.getLogger("").log(
                                            Level.SEVERE,
                                            "Error while reading XML workspace persistence provider '" +
                                                provider.getIdentifier() + "'",
                                            e);
                                    }
                                } else if (provider instanceof WorkspaceBytesPersistenceProvider) {
                                    readWorkspaceChildrenBytes((WorkspaceBytesPersistenceProvider) provider, workspace,
                                        zip);
                                }
                                Progress.progress(progressTicket);
                                if (cancel) {
                                    break;
                                }
                            }
                        }
                        if (cancel) {
                            break;
                        }
                    }
                }
                Progress.switchToIndeterminate(progressTicket);

                //Add project
                if (!cancel) {

                    //Set current workspace
                    for (Workspace workspace : project.getWorkspaces()) {
                        if (workspace.isOpen()) {
                            project.setCurrentWorkspace(workspace);
                            break;
                        }
                    }

                    Progress.finish(progressTicket);
                    return project;
                }
            } finally {
                if (zip != null) {
                    zip.close();
                }
            }
        } catch (Exception ex) {
            Progress.finish(progressTicket);

            if (ex instanceof GephiFormatException) {
                throw (GephiFormatException) ex;
            } else if (ex instanceof LegacyGephiFormatException) {
                throw (LegacyGephiFormatException) ex;
            } else if (ex instanceof FileNotFoundException) {
                throw new RuntimeException(ex);
            }
            throw new GephiFormatException(GephiReader.class, ex);
        }
        Progress.finish(progressTicket);
        return null;
    }

    private ProjectImpl readProject(ZipFile zipFile, ProjectsImpl projects) throws Exception {
        ZipEntry entry = zipFile.getEntry("Project_xml");
        if (entry == null) {
            // Try legacy
            entry = zipFile.getEntry("Project");
            if (entry != null) {
                throw new LegacyGephiFormatException();
            } else {
                throw new GephiFormatException(LoadTask.class,
                    new RuntimeException("Project can't be found in the zip"));
            }
        }
        InputStream is = null;
        try {
            is = zipFile.getInputStream(entry);
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
                    public void report(String message, String errorType, Object relatedInformation,
                                       Location location) throws XMLStreamException {
                    }
                });
                isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                filterReader = new Xml10FilterReader(isReader);
                reader = inputFactory.createXMLStreamReader(filterReader);

                ProjectImpl project = GephiReader.readProject(reader, projects);
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

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private WorkspaceImpl readWorkspace(ProjectImpl project, String entryName, ZipFile zipFile) throws Exception {
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry != null) {
            InputStream is = null;
            try {
                is = zipFile.getInputStream(entry);

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
                        public void report(String message, String errorType, Object relatedInformation,
                                           Location location) throws XMLStreamException {
                        }
                    });
                    isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                    filterReader = new Xml10FilterReader(isReader);
                    reader = inputFactory.createXMLStreamReader(filterReader);

                    return GephiReader.readWorkspace(reader, project);
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
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
        return null;
    }

    private void readWorkspaceChildrenXML(WorkspaceXMLPersistenceProvider persistenceProvider, Workspace workspace,
                                          ZipFile zipFile) throws Exception {
        String identifier = persistenceProvider.getIdentifier();
        ZipEntry entry = zipFile.getEntry("Workspace_" + workspace.getId() + "_" + identifier + "_xml");
        if (entry != null) {
            InputStream is = null;
            try {
                is = zipFile.getInputStream(entry);

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
                        public void report(String message, String errorType, Object relatedInformation,
                                           Location location) throws XMLStreamException {
                        }
                    });
                    isReader = new InputStreamReader(is, StandardCharsets.UTF_8);
                    filterReader = new Xml10FilterReader(isReader);
                    reader = inputFactory.createXMLStreamReader(filterReader);

                    persistenceProvider.readXML(reader, workspace);
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
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    private void readWorkspaceChildrenBytes(WorkspaceBytesPersistenceProvider persistenceProvider, Workspace workspace,
                                            ZipFile zipFile) throws Exception {
        String identifier = persistenceProvider.getIdentifier();
        ZipEntry entry = zipFile.getEntry("Workspace_" + workspace.getId() + "_" + identifier + "_bytes");
        if (entry != null) {
            InputStream is = null;
            DataInputStream stream = null;
            try {
                is = new BufferedInputStream(zipFile.getInputStream(entry));
                stream = new DataInputStream(is);
                persistenceProvider.readBytes(stream, workspace);
            } finally {
                if (stream != null) {
                    stream.close();
                }
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
