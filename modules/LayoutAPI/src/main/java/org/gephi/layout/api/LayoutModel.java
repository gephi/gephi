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

package org.gephi.layout.api;

import java.beans.PropertyChangeListener;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.project.api.Workspace;

/**
 * Layout model contains data and flags relative to the layout execution and
 * user interface. There is one model per {@link Workspace}
 * <p>
 * <code>PropertyChangeListener</code> can be used to receive events about
 * a change in the model. The events are the following:
 * <ul>
 *     <li><code>SELECTED_LAYOUT</code> : when the selected layout changes</li>
 *     <li><code>RUNNING</code> : when the running flag changes</li>
 *     <li><code>DEFAULTS_APPLIED</code> : when the default properties are applied to the layout</li>
 * </ul>
 *
 * @author Mathieu Bastian
 */
public interface LayoutModel {

    String SELECTED_LAYOUT = "selectedLayout";
    String RUNNING = "running";
    String DEFAULTS_APPLIED = "defaultsApplied";

    /**
     * Returns the currently selected layout or <code>null</code> if no
     * layout is selected.
     */
    Layout getSelectedLayout();

    /**
     * Return a layout instance for the given <code>layoutBuilder</code>. If
     * saved properties exists, the layout properties values are set. Values
     * are default if it is the first time this layout is built.
     * <p>
     * Use this method instead of <code>LayoutBuilder.buildLayout()</code>
     * directly.
     *
     * @param layoutBuilder the layout builder
     * @return the layout build from <code>layoutBuilder</code> with formely
     * saved properties.
     */
    Layout getLayout(LayoutBuilder layoutBuilder);

    /**
     * Returns the builder used for building the currently selected layout or
     * <code>null</code> if no layout is selected.
     */
    LayoutBuilder getSelectedBuilder();

    /**
     * Returns <code>true</code> if a layout is currently running, <code>false</code>
     * otherwise.
     */
    boolean isRunning();

    /**
     * Add a property change listener for this model. The <code>listener</code>
     * is notified when layout is selected and when running flag change.
     *
     * @param listener a property change listener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Remove listerner.
     *
     * @param listener a property change listener.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
