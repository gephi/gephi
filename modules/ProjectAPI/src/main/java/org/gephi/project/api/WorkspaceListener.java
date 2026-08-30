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

/**
 * Workspace event listener.
 * <p>
 * <b>Threading:</b> all the methods below are invoked synchronously, on the thread that is performing
 * the project operation. That thread is never the Event Dispatch Thread, and two callbacks may well
 * arrive on two different threads, so any state shared with other threads must be volatile, atomic or
 * otherwise guarded.
 * <p>
 * Implementations must post any user interface work with <code>SwingUtilities.invokeLater</code> and
 * return promptly: they run inside the operation and delay it. In particular they must never call
 * <code>SwingUtilities.invokeAndWait</code>, or wait on the Event Dispatch Thread in any other way,
 * as that deadlocks whenever the EDT is itself waiting for the operation.
 *
 * @author Mathieu Bastian
 */
public interface WorkspaceListener {

    /**
     * Notify a workspace has been created.
     * <p>
     * This is where a module typically creates its model for the workspace, so it is called before
     * any other callback for that workspace and must complete before the workspace is usable.
     *
     * @param workspace the workspace that was created
     */
    void initialize(Workspace workspace);

    /**
     * Notify a workspace has become the selected workspace.
     * <p>
     * Refreshing the interface to match the new selection has to be deferred to the Event Dispatch
     * Thread.
     *
     * @param workspace the workspace that was made current workspace
     */
    void select(Workspace workspace);

    /**
     * Notify another workspace will be selected. The <code>select()</code>
     * always follows, unless the project is being closed.
     *
     * @param workspace the workspace that is currently the selected workspace
     */
    void unselect(Workspace workspace);

    /**
     * Notify a workspace will be closed, all data must be destroyed.
     *
     * @param workspace the workspace that is to be closed
     */
    void close(Workspace workspace);

    /**
     * Notify no more workspace is currently selected, the project is empty.
     * <p>
     * <code>close()</code> is called beforehand for each workspace.
     */
    void disable();
}
