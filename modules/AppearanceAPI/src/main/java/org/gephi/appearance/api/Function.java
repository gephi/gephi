/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance.api;

import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;

/**
 * Functions represent the various transformations that can be applied to the
 * graph elements. Each function is specific to an element class (i.e. nodes or
 * edges), to a specific transformer and to a specific source (e.g. a ranking or
 * a partition).
 * <p>
 * This interface has sub-interfaces specific to the type of function.
 */
public interface Function {

    /**
     * Transforms the given element.
     *
     * @param element element to transform
     * @param graph the graph this element belongs to
     */
    public void transform(Element element, Graph graph);

    /**
     * Returns the transformer associated with this function.
     *
     * @param <T> transformer class
     * @return transformer
     */
    public <T extends Transformer> T getTransformer();

    /**
     * Returns the transformer user interface associated with this function.
     *
     * @return transformer UI or null if not found
     */
    public TransformerUI getUI();

    /**
     * Returns true if this function is a simple function.
     * <p>
     * If true, this instance can be casted to <code>SimpleFunction</code>.
     *
     * @return true if partition, false otherwise
     */
    public boolean isSimple();

    /**
     * Returns true if this function is based on attribute column.
     * <p>
     * If true, this instance can be casted to <code>AttributeFunction</code>.
     *
     * @return true if attribute, false otherwise
     */
    public boolean isAttribute();

    /**
     * Returns true if this function is a ranking function.
     * <p>
     * If true, this instance can be casted to <code>RankingFunction</code>.
     *
     * @return true if ranking, false otherwise
     */
    public boolean isRanking();

    /**
     * Returns true if this function is a partition function.
     * <p>
     * If true, this instance can be casted to <code>PartitionFunction</code>.
     *
     * @return true if partition, false otherwise
     */
    public boolean isPartition();

    /**
     * Returns the graph this function is based on.
     *
     * @return graph
     */
    public Graph getGraph();

    /**
     * Returns the element class this function will be applied to.
     *
     * @return element class
     */
    public Class<? extends Element> getElementClass();
}
