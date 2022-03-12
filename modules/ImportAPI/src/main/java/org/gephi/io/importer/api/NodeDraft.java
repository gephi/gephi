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

package org.gephi.io.importer.api;

/**
 * Draft node, hosted by import containers to represent nodes found when
 * importing. <code>Processors</code> decide if this node will finally be
 * appended to the graph or not.
 *
 * @author Mathieu Bastian
 * @see ContainerLoader
 */
public interface NodeDraft extends ElementDraft {

    /**
     * Returns this node's X position.
     *
     * @return x position
     */
    float getX();

    /**
     * Sets this node's X position.
     *
     * @param x x position
     */
    void setX(float x);

    /**
     * Returns this node's Y position.
     *
     * @return y position
     */
    float getY();

    /**
     * Sets this node's Y position.
     *
     * @param y y position
     */
    void setY(float y);

    /**
     * Returns this node's Z position.
     *
     * @return z position
     */
    float getZ();

    /**
     * Sets this node's Z position.
     *
     * @param z z position
     */
    void setZ(float z);

    /**
     * Returns this node's size.
     *
     * @return size
     */
    float getSize();

    /**
     * Sets this node's size.
     *
     * @param size size
     */
    void setSize(float size);

    /**
     * Returns whether this node's position is fixed.
     * <p>
     * Default is false.
     *
     * @return true if fixed, false otherwise
     */
    boolean isFixed();

    /**
     * Sets whether this node's position is fixed.
     * <p>
     * This flag is used during layout to settle some specific nodes. If set at
     * true, the layout algorithms won't modify the node's position.
     *
     * @param fixed true if fixed, false otherwise
     */
    void setFixed(boolean fixed);

    /**
     * Returns true if this node has been automatically created.
     *
     * @return true if automatically created, false otherwise
     */
    boolean isCreatedAuto();
}
