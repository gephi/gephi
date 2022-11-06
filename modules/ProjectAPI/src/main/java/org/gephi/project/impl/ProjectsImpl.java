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

package org.gephi.project.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.gephi.project.api.Project;
import org.gephi.project.api.Projects;
import org.gephi.project.io.SaveTask;
import org.openide.util.NbBundle;

/**
 * @author Mathieu Bastian
 */
public class ProjectsImpl implements Projects {

    //Project
    private final List<ProjectImpl> projects;
    //Workspace ids
    private ProjectImpl currentProject;

    public ProjectsImpl() {
        projects = new ArrayList<>();
    }

    public void addProject(ProjectImpl project) {
        synchronized (projects) {
            if (!projects.contains(project)) {
                projects.add(project);
            } else {
                throw new IllegalArgumentException("The project " + project.getUniqueIdentifier() + " already exists");
            }
        }
    }

    public boolean containsProject(Project project) {
        synchronized (projects) {
            return projects.contains(project);
        }
    }

    public ProjectImpl getProjectByIdentifier(String identifier) {
        synchronized (projects) {
            for (Project p : projects) {
                if (p.getUniqueIdentifier().equals(identifier)) {
                    return (ProjectImpl) p;
                }
            }
        }
        return null;
    }

    public void addOrReplaceProject(ProjectImpl project) {
        synchronized (projects) {
            if (!projects.contains(project)) {
                projects.add(project);
            } else {
                projects.remove(project);
                projects.add(project);
            }
        }
    }

    public void removeProject(ProjectImpl project) {
        synchronized (projects) {
            projects.remove(project);
        }
    }

    @Override
    public ProjectImpl[] getProjects() {
        synchronized (projects) {
            ProjectImpl[] res = projects.toArray(new ProjectImpl[0]);
            Arrays.sort(res);
            return res;
        }
    }

    @Override
    public ProjectImpl getCurrentProject() {
        synchronized (projects) {
            return currentProject;
        }
    }

    public void setCurrentProject(ProjectImpl currentProject) {
        synchronized (projects) {
            this.currentProject = currentProject;
            if (currentProject != null) {
                currentProject.open();
            }
        }
    }

    @Override
    public boolean hasCurrentProject() {
        synchronized (projects) {
            return currentProject != null;
        }
    }

    public void closeCurrentProject() {
        synchronized (projects) {
            if (currentProject != null) {
                currentProject.close();
            }
            this.currentProject = null;
        }
    }

    protected String nextUntitledProjectName() {
        int i = 0;
        while (true) {
            final String name =
                NbBundle.getMessage(ProjectImpl.class, "Project.default.prefix") + (i > 0 ? " " + i : "");
            if (projects.stream().noneMatch((p) -> (p.getName().equals(name)))) {
                return name;
            }
            i++;
        }
    }

    @Override
    public void saveProjects(File file) throws IOException {
        synchronized (projects) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                XMLStreamWriter writer = SaveTask.newXMLWriter(fos);
                writer.writeStartDocument("UTF-8", "1.0");
                writer.writeStartElement("projects");
                for (ProjectImpl p : getProjects()) {
                    if (!p.hasFile() || (p.hasFile() && p.getFile().exists())) {
                        writer.writeStartElement("project");
                        if (p.hasFile()) {
                            writer.writeAttribute("file", p.getFile().getAbsolutePath());
                        }
                        writer.writeAttribute("id", p.getUniqueIdentifier());
                        writer.writeAttribute("name", p.getName());
                        if (p.getLastOpened() != null) {
                            writer.writeAttribute("lastOpened", String.valueOf(p.getLastOpened().toEpochMilli()));
                        }
                        writer.writeEndElement();
                    } else {
                        Logger.getLogger(ProjectsImpl.class.getName())
                            .warning("Project " + p.getName() + " file does not exist");
                    }
                }
                writer.writeEndDocument();
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        }
    }

    @Override
    public void loadProjects(File file) throws IOException {
        synchronized (projects) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            if (inputFactory.isPropertySupported("javax.xml.stream.isValidating")) {
                inputFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            }
            try (FileInputStream fis = new FileInputStream(file)) {
                XMLStreamReader reader = inputFactory.createXMLStreamReader(fis, "UTF-8");

                boolean end = false;
                while (reader.hasNext() && !end) {
                    int type = reader.next();

                    switch (type) {
                        case XMLStreamReader.START_ELEMENT:
                            String name = reader.getLocalName();
                            if ("project".equalsIgnoreCase(name)) {
                                String filePath = reader.getAttributeValue(null, "file");
                                String id = reader.getAttributeValue(null, "id");
                                String projectName = reader.getAttributeValue(null, "name");
                                String lastOpened = reader.getAttributeValue(null, "lastOpened");

                                if (filePath == null || new File(filePath).exists()) {
                                    ProjectImpl project = new ProjectImpl(id, projectName);
                                    if (filePath != null) {
                                        project.setFile(new File(filePath));
                                    }
                                    if (lastOpened != null) {
                                        project.setLastOpened(Instant.ofEpochMilli(Long.parseLong(lastOpened)));
                                    }
                                    addOrReplaceProject(project);
                                } else {
                                    Logger.getLogger(ProjectsImpl.class.getName())
                                        .warning("Project " + projectName + " file does not exist");
                                }
                            }
                            break;
                        case XMLStreamReader.END_ELEMENT:
                            if ("projects".equalsIgnoreCase(reader.getLocalName())) {
                                end = true;
                            }
                            break;
                    }
                }
                reader.close();
            } catch (XMLStreamException ex) {
                throw new IOException(ex);
            }
        }
    }
}
