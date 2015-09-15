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
package org.gephi.tools.spi;

/**
 * Tools are functions for interacting with user inputs on the visualization
 * window.
 * <p>
 * A tool receive events from visualization window when it is currently the
 * selected tool. The visualization window toolbar presents all available tools
 * implementations.
 * <p>
 * <b>Example:</b> A <i>Brush</i> tool colors clicked nodes.
 * <h3>How-to create a tool implementation</h3>
 * <ol><li>Create a class which implement <code>Tool</code> interface</li>
 * <li>Add the following annotation to your class to be declared as a new
 * implementation <code>@ServiceProvider(service=Tool.class)</code></li>
 * <li>Declare {@link ToolEventListener} instances for specifying how the tool
 * is interacting with user input like node click or mouse drag.</li>
 * <li>Provide a {@link ToolUI} instance for giving a name and an icon to your
 * tool.</li></ol>
 *
 * @author Mathieu Bastian
 */
public interface Tool {

    /**
     * Notify when this tool is selected.
     */
    public void select();

    /**
     * Notify when this tool is unselected.
     */
    public void unselect();

    /**
     * Returns the declared tool listeners for this tool. Tool listeners says
     * how the tool is interacting with user input on the visualization window.
     *
     * @return tool listeners declared for this tool implementation
     */
    public ToolEventListener[] getListeners();

    /**
     * Returns <code>ToolUI</code> instance for this tool.
     *
     * @return the user interface attributes for this tool
     */
    public ToolUI getUI();

    /**
     * Returns the tool type of selection interaction.
     *
     * @return the tool type of selection interaction
     */
    public ToolSelectionType getSelectionType();
}
