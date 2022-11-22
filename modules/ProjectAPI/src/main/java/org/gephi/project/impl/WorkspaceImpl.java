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

import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceMetaData;
import org.gephi.project.spi.Controller;
import org.gephi.project.spi.Model;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Mathieu Bastian
 */
public class WorkspaceImpl implements Workspace {

    private final transient InstanceContent instanceContent;
    private final transient Lookup lookup;
    private final int id;
    private final ProjectImpl project;
    private final WorkspaceInformationImpl workspaceInformation;
    private final WorkspaceMetaDataImpl workspaceMetaData;

    public WorkspaceImpl(ProjectImpl project, int id) {
        this(project, id, NbBundle.getMessage(WorkspaceImpl.class, "Workspace.default.prefix") + " " + id);
    }

    public WorkspaceImpl(ProjectImpl project, int id, String name, Object... objectsForLookup) {
        this.instanceContent = new InstanceContent();
        this.lookup = new AbstractLookup(instanceContent);
        this.id = id;
        this.project = project;

        //Init Default Content
        workspaceInformation = new WorkspaceInformationImpl(name);
        instanceContent.add(workspaceInformation);
        for (Object o : objectsForLookup) {
            instanceContent.add(o);
        }

        workspaceMetaData = new WorkspaceMetaDataImpl();

        // Models
        Lookup.getDefault().lookupAll(Controller.class).forEach(c -> {
            Model model = c.newModel(this);
            add(model);
        });
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
    public ProjectImpl getProject() {
        return project;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isOpen() {
        return workspaceInformation.isOpen();
    }

    @Override
    public boolean isClosed() {
        return workspaceInformation.isClosed();
    }

    @Override
    public boolean isInvalid() {
        return workspaceInformation.isInvalid();
    }

    protected void close() {
        workspaceInformation.close();
    }

    protected void open() {
        workspaceInformation.open();
    }

    public String getName() {
        return lookup.lookup(WorkspaceInformationImpl.class).getName();
    }

    @Override
    public boolean hasSource() {
        return workspaceInformation.hasSource();
    }

    @Override
    public String getSource() {
        return workspaceInformation.getSource();
    }

    @Override
    public WorkspaceMetaData getWorkspaceMetadata() {
        return workspaceMetaData;
    }

    @Override
    public String toString() {
        WorkspaceInformationImpl information = lookup.lookup(WorkspaceInformationImpl.class);
        if (information != null) {
            return information.getName();
        }
        return "null";
    }
}
