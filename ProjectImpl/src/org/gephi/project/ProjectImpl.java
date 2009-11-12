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
package org.gephi.project;

import java.io.Serializable;
import org.gephi.project.api.Project;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class ProjectImpl implements Project, Lookup.Provider, Serializable {

    //Lookup
    private transient InstanceContent instanceContent;
    private transient AbstractLookup lookup;

    public ProjectImpl() {
        init();
    }

    public void init() {
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);

        //Init Default Content
        ProjectMetaDataImpl metaDataImpl = new ProjectMetaDataImpl();
        add(metaDataImpl);
        ProjectInformationImpl projectInformationImpl = new ProjectInformationImpl(this);
        add(projectInformationImpl);
        WorkspaceProviderImpl workspaceProviderImpl = new WorkspaceProviderImpl(this);
        add(workspaceProviderImpl);
    }

    public void add(Object instance) {
        instanceContent.add(instance);
    }

    public void remove(Object instance) {
        instanceContent.remove(instance);
    }

    public Lookup getLookup() {
        return lookup;
    }
}
