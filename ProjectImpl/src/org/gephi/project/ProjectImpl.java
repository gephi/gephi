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

import java.io.File;
import org.gephi.workspace.WorkspaceImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.io.project.GephiDataObject;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectMetaData;
import org.gephi.workspace.api.Workspace;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Mathieu Bastian
 */
public class ProjectImpl implements Project, Lookup.Provider, Serializable {

    public enum Status {

        NEW, OPEN, CLOSED, INVALID
    };
    private static int count = 0;

    //Atributes
    private String name;
    private Status status = Status.CLOSED;
    private transient GephiDataObject dataObject;
    protected String absolutePath;
    private ProjectMetaDataImpl metaData;

    //Workspaces
    private transient List<Workspace> workspaces;
    private transient Workspace currentWorkspace;

    //Lookup
    private transient InstanceContent instanceContent;
    private transient AbstractLookup lookup;
    private transient List<ChangeListener> listeners;

    public ProjectImpl() {
        name = "Project " + (count++);
        init();
    }

    public void init() {
        metaData = new ProjectMetaDataImpl();
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);
        workspaces = new ArrayList<Workspace>();
        listeners = new ArrayList<ChangeListener>();
        status = Status.CLOSED;
        if (absolutePath != null) {
            FileObject fo = FileUtil.toFileObject(new File(absolutePath));
            DataObject dataObj;
            try {
                dataObj = DataObject.find(fo);
                if (dataObj.isValid()) {
                    dataObject = (GephiDataObject) dataObj;
                    dataObject.setProject(this);
                } else {
                    this.status = Status.INVALID;
                }
            } catch (DataObjectNotFoundException ex) {
                ex.printStackTrace();
                this.status = Status.INVALID;
            }
        }
    }

    /*public void reloadFromFile() {
    if (dataObject != null) {
    if (dataObject.isValid()) {
    dataObject.setProject(this);
    dataObject.load();
    } else {
    this.status = Status.INVALID;
    }
    }
    }*/
    @Override
    public WorkspaceImpl newWorkspace() {
        if (workspaces == null) {
            workspaces = new ArrayList<Workspace>();
        }
        WorkspaceImpl workspace = new WorkspaceImpl();
        workspace.setProject(this);
        workspaces.add(workspace);
        instanceContent.add(workspace);
        return workspace;
    }

    public void addWorkspace(Workspace workspace) {
        workspace.setProject(this);
        workspaces.add(workspace);
        instanceContent.add(workspace);
    }

    @Override
    public void removeWorkspace(Workspace workspace) {
        workspaces.remove(workspace);
        instanceContent.remove(workspace);
    }

    @Override
    public Workspace getCurrentWorkspace() {
        return currentWorkspace;
    }

    @Override
    public Workspace[] getWorkspaces() {
        return workspaces.toArray(new Workspace[0]);
    }

    @Override
    public void setCurrentWorkspace(Workspace currentWorkspace) {
        this.currentWorkspace = (WorkspaceImpl) currentWorkspace;
    }

    public boolean hasCurrentWorkspace() {
        return currentWorkspace != null;
    }

    @Override
    public String toString() {
        return name;
    }

    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void open() {
        this.status = Status.OPEN;
        fireChangeEvent();
    }

    @Override
    public void close() {
        this.status = Status.CLOSED;
        fireChangeEvent();
    }

    //PROPERTIES
    @Override
    public boolean isOpen() {
        return status == Status.OPEN;
    }

    @Override
    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    @Override
    public boolean isInvalid() {
        return status == Status.INVALID;
    }

    @Override
    public boolean hasFile() {
        return dataObject != null;
    }

    public String getFileName() {
        if (dataObject == null) {
            return "";
        } else {
            return dataObject.getPrimaryFile().getNameExt();
        }
    }

    @Override
    public void setName(String name) {
        this.name = name;
        fireChangeEvent();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GephiDataObject getDataObject() {
        return dataObject;
    }

    @Override
    public void setDataObject(DataObject dataObject) {
        this.dataObject = (GephiDataObject) dataObject;
        this.absolutePath = dataObject.getPrimaryFile().getPath();
        fireChangeEvent();
    }

    //EVENTS
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    public ProjectMetaData getMetaData() {
        return metaData;
    }

    /**
     * Meta-Data inner class
     */
    private class ProjectMetaDataImpl implements ProjectMetaData, Serializable {

        private String author;
        private String title = "";
        private String keywords = "";
        private String description = "";

        public ProjectMetaDataImpl() {
            String username = System.getProperty("user.name");
            if (username != null) {
                author = username;
            }
        }

        public String getAuthor() {
            return author;
        }

        public String getDescription() {
            return description;
        }

        public String getKeywords() {
            return keywords;
        }

        public String getTitle() {
            return title;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
