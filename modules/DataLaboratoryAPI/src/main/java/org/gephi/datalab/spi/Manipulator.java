/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.spi;

import javax.swing.Icon;
import org.gephi.datalab.spi.nodes.NodesManipulator;

/**
 * <p>General and abstract manipulation action to use for Data Laboratory table UI and other actions like Graph context menu items in Overview.</p>
 * <p>Different subtypes of manipulators are defined for every type of action in the UI.</p>
 * <p>All manipulator types are able to:</p>
 * <ul>
 *  <li>Execute an action</li>
 *  <li>Provide a name, description, type and order of appearance (position in group of its type)</li>
 *  <li>Indicate wether they have to be executable (enabled in the context menu) or not</li>
 *  <li>Provide and UI or not</li>
 *  <li>Provide and icon or not</li>
 * </ul>
 * <p>Used for different manipulators such as NodesManipulator, EdgesManipulator and GeneralActionsManipulator.</p>
 * <p>The only methods that are called before setting up a manipulator (subtypes have special setup methods) with the data are getType and getPosition.
 * This way, the other methods behaviour can depend on the data that has been setup before</p>
 * @see NodesManipulator
 * @author Eduardo Ramos
 */
public interface Manipulator {

    /**
     * Execute this Manipulator.
     * It will operate with data like nodes and edges previously setup for the type of manipulator.
     */
    void execute();

    /**
     * <p>Return name to show for this Manipulator on the ui.</p>
     * <p>Implementations can provide different names depending on the data this
     * Manipulator has (for example depending on the number of nodes in a NodesManipulator).</p>
     * @return Name to show at current time and conditions
     */
    String getName();

    /**
     * Description of the Manipulator.
     * @return Description
     */
    String getDescription();

    /**
     * Indicates if this Manipulator has to be executable.
     * Implementations should evaluate the current data and conditions.
     * @return True if it has to be executable, false otherwise
     */
    boolean canExecute();

    /**
     * Returns a ManipulatorUI for this Manipulator if it needs one.
     * @return ManipulatorUI for this Manipulator or null
     */
    ManipulatorUI getUI();

    /**
     * Type of manipulator. This is used for separating the manipulators
     * in groups when shown, using popup separators. First types to show will be the lesser.
     * @return Type of this manipulator
     */
    int getType();

    /**
     * Returns a position value that indicates the position
     * of this Manipulator in its type group. Less means upper.
     * @return This Manipulator position
     */
    int getPosition();

    /**
     * Returns an icon for this manipulator if necessary.
     * @return Icon for the manipulator or null
     */
    Icon getIcon();
}
