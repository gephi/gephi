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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectMetaData;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Mathieu Bastian
 */
public class ProjectImpl implements Project, Comparable<ProjectImpl>, Lookup.Provider {

    //Workspace ids
    private final AtomicInteger workspaceIds;
    //Lookup
    private final transient InstanceContent instanceContent;
    private final transient AbstractLookup lookup;

    private final WorkspaceProviderImpl workspaceProvider;
    private final ProjectInformationImpl projectInformation;
    private final ProjectMetaDataImpl projectMetaData;
    private final String uniqueIdentifier;

    private Instant lastOpened;

    public ProjectImpl(String name) {
        this(UUID.randomUUID().toString(), name);
    }

    public ProjectImpl(String uniqueIdentifier, String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (uniqueIdentifier == null || uniqueIdentifier.isEmpty()) {
            throw new IllegalArgumentException("Project unique identifier cannot be null or empty");
        }
        this.uniqueIdentifier = uniqueIdentifier;
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);
        workspaceIds = new AtomicInteger(1);

        workspaceProvider = new WorkspaceProviderImpl(this);
        projectInformation = new ProjectInformationImpl(this, name);
        projectMetaData = new ProjectMetaDataImpl();
        instanceContent.add(projectMetaData);
        instanceContent.add(projectInformation);
        instanceContent.add(workspaceProvider);
    }

    @Override
    public void add(Object instance) {
        instanceContent.add(instance);
    }

    @Override
    public void remove(Object instance) {
        instanceContent.remove(instance);
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public WorkspaceImpl getCurrentWorkspace() {
        return workspaceProvider.getCurrentWorkspace();
    }

    @Override
    public boolean hasCurrentWorkspace() {
        return workspaceProvider.hasCurrentWorkspace();
    }

    public Instant getLastOpened() {
        return lastOpened;
    }

    public void setCurrentWorkspace(Workspace workspace) {
        workspaceProvider.setCurrentWorkspace(workspace);
    }

    public WorkspaceImpl newWorkspace() {
        return workspaceProvider.newWorkspace(workspaceProvider.getProject().nextWorkspaceId());
    }

    public WorkspaceImpl newWorkspace(int id) {
        return workspaceProvider.newWorkspace(id);
    }

    public WorkspaceImpl newWorkspace(int id, Object... objectsForLookup) {
        return workspaceProvider.newWorkspace(id, objectsForLookup);
    }

    @Override
    public List<Workspace> getWorkspaces() {
        return Collections.unmodifiableList(Arrays.asList(workspaceProvider.getWorkspaces()));
    }

    @Override
    public Workspace getWorkspace(int id) {
        return workspaceProvider.getWorkspace(id);
    }

    protected void setLastOpened() {
        lastOpened = LocalDateTime.now().toInstant(ZoneOffset.UTC);
    }

    protected void setLastOpened(Instant lastOpened) {
        this.lastOpened = lastOpened;
    }

    protected void open() {
        setLastOpened();
        projectInformation.open();
    }

    protected void close() {
        projectInformation.close();
        workspaceProvider.purge();
    }

    @Override
    public boolean isOpen() {
        return projectInformation.isOpen();
    }

    @Override
    public boolean isClosed() {
        return projectInformation.isClosed();
    }

    @Override
    public boolean isInvalid() {
        return projectInformation.isInvalid();
    }

    @Override
    public String getName() {
        return projectInformation.getName();
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public boolean hasFile() {
        return projectInformation.hasFile();
    }

    @Override
    public String getFileName() {
        return projectInformation.getFileName();
    }

    @Override
    public File getFile() {
        return projectInformation.getFile();
    }

    protected void setFile(File file) {
        projectInformation.setFile(file);
    }

    public int nextWorkspaceId() {
        return workspaceIds.getAndIncrement();
    }

    public int getWorkspaceIds() {
        return workspaceIds.get();
    }

    public void setWorkspaceIds(int ids) {
        workspaceIds.set(ids);
    }

    @Override
    public ProjectMetaData getProjectMetadata() {
        return projectMetaData;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectImpl project = (ProjectImpl) o;

        return uniqueIdentifier.equals(project.uniqueIdentifier);
    }

    @Override
    public int hashCode() {
        return uniqueIdentifier.hashCode();
    }

    @Override
    public String toString() {
        return "ProjectImpl {" +
            "uniqueIdentifier='" + uniqueIdentifier + '\'' +
            '}';
    }

    @Override
    public int compareTo(ProjectImpl o) {
        if (o.getLastOpened() == null) {
            return -1;
        } else if (getLastOpened() == null) {
            return 1;
        } else {
            return o.getLastOpened().compareTo(getLastOpened());
        }
    }
}
