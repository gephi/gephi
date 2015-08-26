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
package org.gephi.project.api;

import java.beans.PropertyChangeListener;

/**
 * Hosts various information about a workspace.
 * <p>
 * Clients can subscribe to changes by using the 
 * {@link #addChangeListener(java.beans.PropertyChangeListener) } method. It
 * triggers the following events:
 * <ul>
 * <li><b>EVENT_OPEN:</b> Workspace opened
 * <li><b>EVENT_CLOSE:</b> Workspace closed
 * <li><b>EVENT_RENAME:</b> Workspace renamed
 * <li><b>EVENT_SET_SOURCE:</b> Workspace source set
 * </ul>
 *
 * @author Mathieu Bastian
 * @see Workspace
 */
public interface WorkspaceInformation {

    public static final String EVENT_OPEN = "open";
    public static final String EVENT_CLOSE = "close";
    public static final String EVENT_RENAME = "rename";
    public static final String EVENT_SET_SOURCE = "setSource";

    /**
     * Returns true if the workspace is open.
     *
     * @return true if open, false otherwise
     */
    public boolean isOpen();

    /**
     * Returns true if the workspace is closed.
     *
     * @return true if closed, false otherwise
     */
    public boolean isClosed();

    /**
     * Returns true if the workspace is invalid.
     *
     * @return true if invalid, false otherwise
     */
    public boolean isInvalid();

    /**
     * Returns the name of the workspace.
     * <p>
     * The name can't be null and has a default value (e.g. Workspace 1).
     *
     * @return the workspace's name
     */
    public String getName();

    /**
     * Returns true if the workspace has a source.
     *
     * @return true if has a source, false otherwise
     */
    public boolean hasSource();

    /**
     * Returns the workspace's source or null if missing.
     *
     * @return workspace's source or null if missing
     */
    public String getSource();

    /**
     * Add change listener.
     *
     * @param listener change listener
     */
    public void addChangeListener(PropertyChangeListener listener);

    /**
     * Remove change listener.
     *
     * @param listener change listener
     */
    public void removeChangeListener(PropertyChangeListener listener);
}
