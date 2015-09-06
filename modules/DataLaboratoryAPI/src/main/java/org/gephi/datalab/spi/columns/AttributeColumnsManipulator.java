
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
package org.gephi.datalab.spi.columns;

import java.awt.Image;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;

/**
 * <p>Manipulation action to use for Data Laboratory column manipulator buttons.</p>
 * <p>This special type of manipulator does not need any builder, implementations can be published simply with
 * <code>@ServiceProvider(service = AttributeColumnsManipulator.class)</code> annotation</p>
 * <p>These are shown as drop down buttons and are able to:</p>
 * <ul>
 * <li>Execute an action with 1 column</li>
 * <li>Provide a name, description, type and order of appearance (position in group of its type)</li>
 * <li>Indicate wether they can be executed on a specific AttributeColumn or not</li>
 * <li>Provide and UI or not</li>
 * <li>Provide and icon or not</li>
 * </ul>
 * @author Eduardo Ramos
 */
public interface AttributeColumnsManipulator {

    /**
     * Execute this AttributeColumnsManipulator with the indicated table and column
     *
     * @param table AttributeTable of the column
     * @param column AttributeColumn of the table to manipulate
     */
    void execute(Table table, Column column);

    /**
     * Return name to show for this AttributeColumnsManipulator on the ui.
     *
     * @return Name to show in UI
     */
    String getName();

    /**
     * Description of the AttributeColumnsManipulator.
     *
     * @return Description
     */
    String getDescription();

    /**
     * Indicates if this AttributeColumnsManipulator can manipulate a specific AttributeColumn.
     *
     * @return True if it can manipulate the column, false otherwise
     */
    boolean canManipulateColumn(Table table, Column column);

    /**
     * Returns a ManipulatorUI for this Manipulator if it needs one.
     *
     * @param table AttributeTable of the column
     * @param column AttributeColumn of the table to manipulate
     * @return ManipulatorUI for this Manipulator or null
     */
    AttributeColumnsManipulatorUI getUI(Table table, Column column);

    /**
     * Type of manipulator. This is used for separating the manipulators in groups when shown. First types to show will be the lesser.
     *
     * @return Type of this manipulator
     */
    int getType();

    /**
     * Returns a position value that indicates the position of this AttributeColumnsManipulator in its type group. Less means upper.
     *
     * @return This AttributeColumnsManipulator position
     */
    int getPosition();

    /**
     * Returns an icon for this AttributeColumnsManipulator if necessary.
     *
     * @return Icon for the manipulator or null
     */
    Image getIcon();
}
