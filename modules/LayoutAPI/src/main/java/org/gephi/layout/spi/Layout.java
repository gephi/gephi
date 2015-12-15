/*
Copyright 2008-2010 Gephi
Authors : Helder Suzuki <heldersuzuki@gephi.org>
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
package org.gephi.layout.spi;

import org.gephi.graph.api.GraphModel;

/**
 * A Layout algorithm should implement the <code>Layout</code> interface to allow the
 * <code>LayoutController</code> to run it properly.
 * <p>
 * See the <code>LayoutBuilder</code> documentation to know how layout should
 * be instanciated.
 * <p>
 * To have fully integrated properties that can be changed in real-time by users,
 * properly define the various <code>LayoutProperty</code> returned by the
 * {@link #getProperties()} method and provide getter and setter for each.
 *
 * @author Helder Suzuki <heldersuzuki@gephi.org>
 * @see LayoutBuilder
 */
public interface Layout {

    /**
     * initAlgo() is called to initialize the algorithm (prepare to run).
     */
    void initAlgo();

    /**
     * Injects the graph model for the graph this Layout should operate on.
     * <p>
     * It's preferable to get <b>visible</b> graph to perform on visualization.
     * @param graphModel    the graph model that the layout is to be working on
     */
    void setGraphModel(GraphModel graphModel);

    /**
     * Run a step in the algorithm, should be called only if canAlgo() returns
     * true.
     */
    void goAlgo();

    /**
     * Tests if the algorithm can run, called before each pass.
     * @return              <code>true</code> if the algorithm can run, <code>
     *                      false</code> otherwise
     */
    boolean canAlgo();

    /**
     * Called when the algorithm is finished (canAlgo() returns false).
     */
    void endAlgo();

    /**
     * The properties for this layout.
     * @return              the layout properties
     * @throws NoSuchMethodException 
     */
    LayoutProperty[] getProperties();

    /**
     * Resets the properties values to the default values.
     */
    void resetPropertiesValues();

    /**
     * The reference to the LayoutBuilder that instanciated this Layout.
     * @return              the reference to the builder that builts this instance
     */
    LayoutBuilder getBuilder();
}
