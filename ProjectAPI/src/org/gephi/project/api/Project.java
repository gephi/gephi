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

package org.gephi.project.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.Workspace;

import org.gephi.project.filetype.GephiDataObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;


/**
 *
 * @author Mathieu
 */
public class Project implements Lookup.Provider, Serializable {

    public enum Status {NEW, OPEN, CLOSED, INVALID};
    private static int count=0;

    //Atributes
    private String name;
    private Status status = Status.CLOSED;
    private GephiDataObject dataObject;

    //Workspaces
    private transient List<Workspace> workspaces;
    private transient Workspace currentWorkspace;
   
    //Lookup
    private transient InstanceContent instanceContent;
    private transient AbstractLookup lookup;
    private transient List<ChangeListener> listeners;

    public Project()
    {
        name = "Project "+(count++);
        instanceContent = new InstanceContent();
        lookup = new AbstractLookup(instanceContent);
        workspaces = new ArrayList<Workspace>();
    }

    public List<Workspace> getWorkspaces() {
        return workspaces;
    }

    public Workspace newWorkspace()
    {
        if(workspaces==null)
            workspaces = new ArrayList<Workspace>();
        Workspace workspace = new Workspace();
        workspace.setProject(this);
        workspaces.add(workspace);
        instanceContent.add(workspace);
        return workspace;
    }

    public void removeWorkspace(Workspace workspace)
    {
        workspaces.remove(workspace);
        instanceContent.remove(workspace);
    }

    public Workspace getCurrentWorkspace() {
        return currentWorkspace;
    }

    public void setCurrentWorkspace(Workspace currentWorkspace) {
        this.currentWorkspace = currentWorkspace;
    }

   public String toString() {
       return name;
   }

    public Lookup getLookup() {
        if(lookup==null)
        {
            instanceContent = new InstanceContent();
            lookup = new AbstractLookup(instanceContent);
        }
        return lookup;
    }

    public boolean isOpen() {
        return status==Status.OPEN;
    }

    public void setOpenStatus()
    {
        this.status = Status.OPEN;
        fireChangeEvent();
    }

    public void setClosedStatus()
    {
        this.status = Status.CLOSED;
        fireChangeEvent();
    }

    public void setNewStatus()
    {
        this.status = Status.NEW;
    }

    public void setName(String name)
    {
        this.name = name;
        fireChangeEvent();
    }

    public String getName() {
        return name;
    }

    public void addChangeListener(ChangeListener listener)
    {
        if(listeners==null)
            listeners = new ArrayList<ChangeListener>();
        listeners.add(listener);
    }

    public void removeChangeListener(ChangeListener listener)
    {
        if(listeners!=null)
            listeners.remove(listener);
    }

    public void fireChangeEvent()
    {
        ChangeEvent event = new ChangeEvent(this);
        for(ChangeListener listener : listeners)
        {
            listener.stateChanged(event);
        }
    }

    public GephiDataObject getDataObject() {
        return dataObject;
    }

    public void setDataObject(GephiDataObject dataObject) {
        this.dataObject = dataObject;
        fireChangeEvent();
    }

    public boolean hasFile()
    {
        return true;
    }
}
