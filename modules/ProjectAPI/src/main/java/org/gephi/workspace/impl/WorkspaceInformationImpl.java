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
package org.gephi.workspace.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.gephi.project.api.WorkspaceInformation;

/**
 *
 * @author Mathieu Bastian
 */
public class WorkspaceInformationImpl implements WorkspaceInformation {

    public enum Status {

        OPEN, CLOSED, INVALID
    }

    private String name;
    private Status status = Status.CLOSED;
    private String source;
    //Lookup
    private final transient List<PropertyChangeListener> listeners = new ArrayList<>();

    public WorkspaceInformationImpl(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        fireChangeEvent(WorkspaceInformation.EVENT_RENAME, oldValue, name);
    }

    public void setSource(String source) {
        String oldValue = this.source;
        this.source = source;
        fireChangeEvent(WorkspaceInformation.EVENT_SET_SOURCE, oldValue, source);
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public boolean hasSource() {
        return source != null;
    }

    public void open() {
        Status oldValue = status;
        status = Status.OPEN;
        fireChangeEvent(WorkspaceInformation.EVENT_OPEN, oldValue, status);
    }

    public void close() {
        Status oldValue = status;
        status = Status.CLOSED;
        fireChangeEvent(WorkspaceInformation.EVENT_CLOSE, oldValue, status);
    }

    public void invalid() {
        this.status = Status.INVALID;
    }

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
