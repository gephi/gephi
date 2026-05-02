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

package org.gephi.visualization.api;

/**
 * Visualization event triggered by user interactions with the graph canvas.
 *
 * @author Mathieu Bastian
 */
public interface VisualizationEvent {

    /**
     * Returns the type of this event.
     *
     * @return the event type
     */
    Type getType();

    /**
     * Returns optional data associated with this event.
     *
     * @return the event data or <code>null</code> if none
     */
    Object getData();

    /**
     * Defines the types of visualization events.
     */
    enum Type {
        /**
         * User started dragging.
         */
        START_DRAG,
        /**
         * User is dragging.
         */
        DRAG,
        /**
         * User stopped dragging.
         */
        STOP_DRAG,
        /**
         * Mouse moved over the canvas.
         */
        MOUSE_MOVE,
        /**
         * Left mouse button was pressed.
         */
        MOUSE_LEFT_PRESS,
        /**
         * Middle mouse button was pressed.
         */
        MOUSE_MIDDLE_PRESS,
        /**
         * Right mouse button was pressed.
         */
        MOUSE_RIGHT_PRESS,
        /**
         * Left mouse button was clicked.
         */
        MOUSE_LEFT_CLICK,
        /**
         * Middle mouse button was clicked.
         */
        MOUSE_MIDDLE_CLICK,
        /**
         * Right mouse button was clicked.
         */
        MOUSE_RIGHT_CLICK,
        /**
         * Left mouse button is being held down.
         */
        MOUSE_LEFT_PRESSING,
        /**
         * Mouse button was released.
         */
        MOUSE_RELEASED,
        /**
         * Node was left-clicked.
         */
        NODE_LEFT_CLICK,
        /**
         * Left mouse button was pressed on a node.
         */
        NODE_LEFT_PRESS,
        /**
         * Left mouse button is being held down on a node.
         */
        NODE_LEFT_PRESSING,
    }
}
