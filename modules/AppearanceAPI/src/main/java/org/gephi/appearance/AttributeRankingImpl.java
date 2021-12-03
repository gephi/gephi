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

package org.gephi.appearance;

import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.ElementIterable;
import org.gephi.graph.api.Estimator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.types.TimeMap;

/**
 * @author mbastian
 */
public class AttributeRankingImpl extends RankingImpl {

    protected final Index index;
    protected final Column column;
    protected final Graph graph;

    public AttributeRankingImpl(Column column, Graph graph, Index index) {
        super();
        this.column = column;
        this.graph = graph;
        this.index = index;
    }

    @Override
    protected void refresh() {
        if (index != null && index.isSortable(column)) {
            min = index.getMinValue(column);
            max = index.getMaxValue(column);
        } else {
            ElementIterable<? extends Element> iterable =
                AttributeUtils.isNodeColumn(column) ? graph.getNodes() : graph.getEdges();

            if (column.isDynamic()) {
                refreshDynamic(iterable);
            } else {
                refreshNotIndexed(iterable);
            }
        }
    }

    protected void refreshDynamic(ElementIterable<? extends Element> iterable) {
        double minN = Double.POSITIVE_INFINITY;
        double maxN = Double.NEGATIVE_INFINITY;

        for (Element el : iterable) {
            if (TimeMap.class.isAssignableFrom(column.getTypeClass())) {
                TimeMap timeMap = (TimeMap) el.getAttribute(column);
                if (timeMap != null) {
                    double numMin =
                        ((Number) timeMap.get(graph.getView().getTimeInterval(), Estimator.MIN)).doubleValue();
                    double numMax =
                        ((Number) timeMap.get(graph.getView().getTimeInterval(), Estimator.MAX)).doubleValue();
                    if (numMin < minN) {
                        minN = numMin;
                    }
                    if (numMax > maxN) {
                        maxN = numMax;
                    }
                }
            }
        }

        min = minN;
        max = maxN;
    }

    protected void refreshNotIndexed(ElementIterable<? extends Element> iterable) {
        double minN = Double.POSITIVE_INFINITY;
        double maxN = Double.NEGATIVE_INFINITY;

        for (Element el : iterable) {
            double num = ((Number) el.getAttribute(column)).doubleValue();
            if (num < minN) {
                minN = num;
            }
            if (num > maxN) {
                maxN = num;
            }
        }

        min = minN;
        max = maxN;
    }

    @Override
    public Number getValue(Element element, Graph gr) {
        return (Number) element.getAttribute(column, gr.getView());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + (this.column != null ? this.column.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttributeRankingImpl other = (AttributeRankingImpl) obj;
        return this.column == other.column || (this.column != null && this.column.equals(other.column));
    }
}
