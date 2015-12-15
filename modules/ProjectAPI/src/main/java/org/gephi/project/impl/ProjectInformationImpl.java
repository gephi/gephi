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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectInformation;

/**
 *
 * @author Mathieu Bastian
 */
public class ProjectInformationImpl implements ProjectInformation {

    public enum Status {

        NEW, OPEN, CLOSED, INVALID
    }

    //Data
    private final Project project;
    private String name;
    private Status status = Status.CLOSED;
    private File file;
    //Event
    private final transient List<PropertyChangeListener> listeners;

    public ProjectInformationImpl(Project project, String name) {
        this.project = project;
        this.name = name;
        listeners = new ArrayList<PropertyChangeListener>();
        status = Status.CLOSED;
    }

    public void open() {
        Status oldStatus = status;
        status = Status.OPEN;
        fireChangeEvent(ProjectInformation.EVENT_OPEN, oldStatus, status);
    }

    public void close() {
        Status oldStatus = status;
        status = Status.CLOSED;
        fireChangeEvent(ProjectInformation.EVENT_CLOSE, oldStatus, status);
    }

    @Override
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
        return file != null;
    }

    @Override
    public String getFileName() {
        if (file == null) {
            return "";
        } else {
            return file.getName();
        }
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        fireChangeEvent(ProjectInformation.EVENT_RENAME, oldName, name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        File oldFile = this.file;
        this.file = file;
        fireChangeEvent(ProjectInformation.EVENT_SET_FILE, oldFile, file);
    }

    //EVENTS
    @Override
    public void addChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    public void fireChangeEvent(String eventName, Object oldValue, Object newValue) {
        if ((oldValue == null && newValue != null) || (oldValue != null && newValue == null)
                || (oldValue != null && !oldValue.equals(newValue))) {
            PropertyChangeEvent event = new PropertyChangeEvent(this, eventName, oldValue, newValue);
            for (PropertyChangeListener listener : listeners) {
                listener.propertyChange(event);
            }
        }
    }
}
