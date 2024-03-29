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

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.gephi.project.api.GephiFormatException;
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.ProjectsImpl;
import org.gephi.project.impl.WorkspaceImpl;
import org.gephi.project.impl.WorkspaceInformationImpl;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;

public class GephiReader {

    static final String VERSION = "0.9";

    public static ProjectImpl readProject(XMLStreamReader reader, ProjectsImpl projects) throws Exception {
        ProjectImpl project = null;
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("projectFile".equalsIgnoreCase(name) || "gephiFile".equalsIgnoreCase(name)) {
                    //Version
                    String version = reader.getAttributeValue(null, "version");
                    if (version == null || version.isEmpty() ||
                        Double.parseDouble(version) < Double.parseDouble(VERSION)) {
                        throw new GephiFormatException(
                            "Gephi project file version must be at least of version " + VERSION);
                    }
                } else if ("project".equalsIgnoreCase(name)) {
                    String projectName = reader.getAttributeValue(null, "name");
                    String projectId = reader.getAttributeValue(null, "id");
                    if (projectId == null) {
                        // Before 0.10 version we didn't have unique project ids
                        project = new ProjectImpl(projectName);
                    } else {
                        if (projects != null) {
                            project = projects.getProjectByIdentifier(projectId);
                        }
                        if (project == null) {
                            project = new ProjectImpl(projectId, projectName);
                        }
                    }

                    project.getLookup().lookup(WorkspaceProviderImpl.class);

                    if (reader.getAttributeValue(null, "ids") != null) {
                        int workspaceIds = Integer.parseInt(reader.getAttributeValue(null, "ids"));
                        project.setWorkspaceIds(workspaceIds);
                    }
                } else if ("metadata".equalsIgnoreCase(name)) {
                    readProjectMetadata(reader, project);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("project".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }

        return project;
    }

    public static WorkspaceImpl readWorkspace(XMLStreamReader reader, ProjectImpl project) throws Exception {
        WorkspaceImpl workspace = null;
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("workspace".equalsIgnoreCase(name)) {
                    //Id
                    Integer workspaceId;
                    if (reader.getAttributeValue(null, "id") == null ||
                        project.getWorkspace(Integer.parseInt(reader.getAttributeValue(null, "id"))) != null) {
                        workspaceId = project.nextWorkspaceId();
                    } else {
                        workspaceId = Integer.parseInt(reader.getAttributeValue(null, "id"));
                    }

                    workspace = project.newWorkspace(workspaceId);
                    WorkspaceInformationImpl info = workspace.getLookup().lookup(WorkspaceInformationImpl.class);

                    //Name
                    info.setName(reader.getAttributeValue(null, "name"));

                    //Status
                    String workspaceStatus = reader.getAttributeValue(null, "status");
                    if (workspaceStatus.equals("open")) {
                        info.open();
                    } else if (workspaceStatus.equals("closed")) {
                        info.close();
                    } else {
                        info.invalid();
                    }
                } else if ("metadata".equalsIgnoreCase(name)) {
                    readWorkspaceMetadata(reader, workspace);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("workspace".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }

        return workspace;
    }

    private static void readWorkspaceMetadata(XMLStreamReader reader, Workspace workspace) throws Exception {
        String property = null;
        while (reader.hasNext()) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                property = reader.getLocalName();
            } else if (eventType.equals(XMLStreamReader.CHARACTERS)) {
                if (property != null && property.equals("description")) {
                    String desc = reader.getText();
                    workspace.getWorkspaceMetadata().setDescription(desc);
                } else if (property != null && property.equals("title")) {
                    String title = reader.getText();
                    workspace.getWorkspaceMetadata().setTitle(title);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("metadata".equalsIgnoreCase(reader.getLocalName())) {
                    return;
                }
            }
        }
    }

    private static void readProjectMetadata(XMLStreamReader reader, ProjectImpl project) throws Exception {
        String property = null;
        while (reader.hasNext()) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                property = reader.getLocalName();
            } else if (eventType.equals(XMLStreamReader.CHARACTERS)) {
                if (property != null) {
                    switch (property) {
                        case "title":
                            project.getProjectMetadata().setTitle(reader.getText());
                            break;
                        case "author":
                            project.getProjectMetadata().setAuthor(reader.getText());
                            break;
                        case "description":
                            project.getProjectMetadata().setDescription(reader.getText());
                            break;
                        case "keywords":
                            project.getProjectMetadata().setKeywords(reader.getText());
                            break;
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("metadata".equalsIgnoreCase(reader.getLocalName())) {
                    return;
                }
            }
        }
    }

    public static void readWorkspaceChildren(Workspace workspace, XMLStreamReader reader,
                                             WorkspaceXMLPersistenceProvider persistenceProvider) throws Exception {
        String identifier = persistenceProvider.getIdentifier();
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if (identifier.equals(name)) {
                    try {
                        persistenceProvider.readXML(reader, workspace);
                    } catch (UnsupportedOperationException e) {
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (identifier.equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }
}
