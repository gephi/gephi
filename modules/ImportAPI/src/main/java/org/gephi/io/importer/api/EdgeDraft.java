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
 * Draft edge, hosted by import containers to represent edges found when
 * importing. <code>Processors</code> decide if this edge will finally be
 * appended to the graph or not.
 *
 * @author Mathieu Bastian
 * @see ContainerLoader
 */
public interface EdgeDraft extends ElementDraft {

    /**
     * Sets this edge's weight.
     * <p>
     * Default is 1.0.
     *
     * @param weight edge's weight
     */
    public void setWeight(double weight);

    /**
     * Returns this edge's weight.
     *
     * @return edge's weight
     */
    public double getWeight();

    /**
     * Sets this edge's type.
     * <p>
     * Edges can have different types but by default all edges have a default,
     * null type. In other words, setting a type is optional.
     *
     * @param type edge type
     */
    public void setType(Object type);

    /**
     * Gets this edge's type.
     * <p>
     * Edges can have different types but by default all edges have a default,
     * null type. In other words, setting a type is optional.
     *
     * @return edge's type or null if unset
     */
    public Object getType();

    /**
     * Sets this edge's direction setting.
     *
     * @param direction edge's direction
     */
    public void setDirection(EdgeDirection direction);

    /**
     * Returns this edge's direction setting.
     *
     * @return edge's direction or null if unset
     */
    public EdgeDirection getDirection();

    /**
     * Sets this edge's source.
     *
     * @param nodeSource node source
     */
    public void setSource(NodeDraft nodeSource);

    /**
     * Sets this edge's target.
     * <p>
     * Self-loops should simply set both source and target with the same node.
     *
     * @param nodeTarget node target
     */
    public void setTarget(NodeDraft nodeTarget);

    /**
     * Get edge's source.
     *
     * @return edge's source or null if unset
     */
    public NodeDraft getSource();

    /**
     * Get edge's target.
     *
     * @return edge's target or null if unset
     */
    public NodeDraft getTarget();

    /**
     * Returns true if this edge is a self-loop.
     * <p>
     * It returns false if the source or target is null.
     *
     * @return true if self-loop, false otherwise
     */
    public boolean isSelfLoop();
}
