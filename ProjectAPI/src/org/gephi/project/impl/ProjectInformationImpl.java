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
package org.gephi.project.impl;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectInformation;
import org.gephi.project.io.GephiDataObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Mathieu Bastian
 */
public class ProjectInformationImpl implements ProjectInformation {

    public enum Status {

        NEW, OPEN, CLOSED, INVALID
    };
    private static int count = 0;
    //Data
    private String name;
    private Status status = Status.CLOSED;
    private GephiDataObject dataObject;
    private Project project;
    //Event
    private transient List<ChangeListener> listeners;

    public ProjectInformationImpl(Project project) {
        this.project = project;
        name = "Project " + (count++);
        init();
    }

    public void init() {
        listeners = new ArrayList<ChangeListener>();
        status = Status.CLOSED;
        if (dataObject != null) {
            if (dataObject.isValid()) {
                dataObject.setProject(project);
            } else {
                this.status = Status.INVALID;
            }
        }
    }

    public void open() {
        this.status = Status.OPEN;
        fireChangeEvent();
    }

    public void close() {
        this.status = Status.CLOSED;
        fireChangeEvent();
    }

    public Project getProject() {
        return project;
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

    @Override
    public String getFileName() {
        if (dataObject == null) {
            return "";
        } else {
            return dataObject.getPrimaryFile().getNameExt();
        }
    }

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

    public void setDataObject(DataObject dataObject) {
        this.dataObject = (GephiDataObject) dataObject;
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
}
