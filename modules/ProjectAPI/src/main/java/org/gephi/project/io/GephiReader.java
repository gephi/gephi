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
import org.gephi.project.api.Workspace;
import org.gephi.project.impl.ProjectControllerImpl;
import org.gephi.project.impl.ProjectImpl;
import org.gephi.project.impl.ProjectsImpl;
import org.gephi.project.impl.WorkspaceProviderImpl;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.gephi.workspace.impl.WorkspaceInformationImpl;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class GephiReader implements Cancellable {

    private boolean cancel = false;
    private WorkspacePersistenceProvider currentProvider;

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    public ProjectImpl readProject(XMLStreamReader reader, ProjectsImpl projects) throws Exception {
        ProjectImpl project = null;
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("projectFile".equalsIgnoreCase(name)) {
                    //Version
                    String version = reader.getAttributeValue(null, "version");
                    if (version == null || version.isEmpty() || Double.parseDouble(version) < 0.7) {
                        throw new GephiFormatException("Gephi project file version must be at least 0.7");
                    }
                } else if ("project".equalsIgnoreCase(name)) {
                    String projectName = reader.getAttributeValue(null, "name");
                    project = new ProjectImpl(projectName);
                    project.getLookup().lookup(WorkspaceProviderImpl.class);

                    if (reader.getAttributeValue(null, "ids") != null) {
                        Integer workspaceIds = Integer.parseInt(reader.getAttributeValue(null, "ids"));
                        project.setWorkspaceIds(workspaceIds);
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("project".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }

        return project;
    }

    public Workspace readWorkspace(XMLStreamReader reader, ProjectImpl project) throws Exception {
        WorkspaceImpl workspace = null;
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("workspaceFile".equalsIgnoreCase(name)) {
                    //Version
                    String version = reader.getAttributeValue(null, "version");
                    if (version == null || version.isEmpty() || Double.parseDouble(version) < 0.7) {
                        throw new GephiFormatException("Gephi project file version must be at least 0.7");
                    }
                } else if ("workspace".equalsIgnoreCase(name)) {
                    //Id
                    Integer workspaceId;
                    if (reader.getAttributeValue(null, "id") == null) {
                        workspaceId = project.nextWorkspaceId();
                    } else {
                        workspaceId = Integer.parseInt(reader.getAttributeValue(null, "id"));
                    }

                    workspace = project.getLookup().lookup(WorkspaceProviderImpl.class).newWorkspace(workspaceId);
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

                    //Hack to set this workspace active, when readers need to use attributes for instance
                    ProjectControllerImpl pc = Lookup.getDefault().lookup(ProjectControllerImpl.class);
                    pc.setTemporaryOpeningWorkspace(workspace);

                    //WorkspacePersistent
                    readWorkspaceChildren(workspace, reader);
                    if (currentProvider != null) {
                        //One provider not correctly closed
                        throw new GephiFormatException("The '" + currentProvider.getIdentifier() + "' persistence provider is not ending read.");
                    }
                    pc.setTemporaryOpeningWorkspace(null);

                    //Current workspace
                    if (info.isOpen()) {
                        WorkspaceProviderImpl workspaces = project.getLookup().lookup(WorkspaceProviderImpl.class);
                        workspaces.setCurrentWorkspace(workspace);
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("workspace".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }

        return workspace;
    }

    public void readWorkspaceChildren(Workspace workspace, XMLStreamReader reader) throws Exception {
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                WorkspacePersistenceProvider pp = PersistenceProviderUtils.getXMLPersistenceProviders().get(name);
                if (pp != null) {
                    currentProvider = pp;
                    try {
                        pp.readXML(reader, workspace);
                    } catch (UnsupportedOperationException e) {
                    }
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("workspace".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                    currentProvider = null;
                }
            }
        }
    }
}
