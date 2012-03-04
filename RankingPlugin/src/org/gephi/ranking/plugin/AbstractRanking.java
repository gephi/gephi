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
package org.gephi.ranking.plugin;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingModel;

/**
 * Abstract ranking implementation, providing min/max storage.
 * <p>
 * It also has convenient static methods:
 * <ul><li><b>refreshMinMax:</b> Refresh the minimum and maximum of the ranking for
 * the given graph.</li>
 * <li><b>getMin:</b> Returns the minimum of a Comparable array.</li>
 * <li><b>getMax:</b> Returns the maximum of a Comparable array.</li></ul>
 * 
 * @author Mathieu Bastian
 */
public abstract class AbstractRanking<Element> implements Ranking<Element> {

    protected final RankingModel rankingModel;
    private final String name;
    protected final String elementType;
    protected Number minimum;
    protected Number maximum;

    public AbstractRanking(String elementType, String name, RankingModel rankingModel) {
        this.elementType = elementType;
        this.rankingModel = rankingModel;
        this.name = name;
    }

    @Override
    public Number getMinimumValue() {
        return minimum;
    }

    @Override
    public Number getMaximumValue() {
        return maximum;
    }

    public void setMinimumValue(Number value) {
        this.minimum = value;
    }

    public void setMaximumValue(Number value) {
        this.maximum = value;
    }

    @Override
    public String getElementType() {
        return elementType;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Refresh the min and max of <code>ranking</code>.
     * @param ranking the ranking to find min and ma
     * @param graph the graph where values are from
     */
    public static void refreshMinMax(AbstractRanking ranking, Graph graph) {
        if (ranking.getElementType().equals(Ranking.NODE_ELEMENT)) {
            List<Comparable> objects = new ArrayList<Comparable>();
            for (Node node : graph.getNodes().toArray()) {
                Comparable value = (Comparable) ranking.getValue(node);
                if (value != null) {
                    objects.add(value);
                }
            }
            ranking.setMinimumValue((Number) getMin(objects.toArray(new Comparable[0])));
            ranking.setMaximumValue((Number) getMax(objects.toArray(new Comparable[0])));
        } else if (ranking.getElementType().equals(Ranking.EDGE_ELEMENT)) {
            List<Comparable> objects = new ArrayList<Comparable>();
            for (Edge edge : graph.getEdges().toArray()) {
                Comparable value = (Comparable) ranking.getValue(edge);
                if (value != null) {
                    objects.add(value);
                }
            }
            ranking.setMinimumValue((Number) getMin(objects.toArray(new Comparable[0])));
            ranking.setMaximumValue((Number) getMax(objects.toArray(new Comparable[0])));
        }
    }

    /**
     * Return the minimum of <code>values</code>. Return <code>NaN</code> if
     * <code>values</code> is empty.
     * @param values the values to find the minimum
     * @return the minimum of <code>values</code> or <code>NaN</code>
     */
    public static Object getMin(Comparable[] values) {
        switch (values.length) {
            case 0:
                return Double.NaN;
            case 1:
                return values[0];
            // values.length > 1
            default:
                Comparable<?> min = values[0];

                for (int index = 1; index < values.length; index++) {
                    Comparable o = values[index];
                    if (o.compareTo(min) < 0) {
                        min = o;
                    }
                }

                return min;
        }
    }

    /**
     * Return the maximum of <code>values</code>. Return <code>NaN</code> if
     * <code>values</code> is empty.
     * @param values the values to find the maximum
     * @return the maximum of <code>values</code> or <code>NaN</code>
     */
    public static Object getMax(Comparable[] values) {
        switch (values.length) {
            case 0:
                return Double.NaN;
            case 1:
                return values[0];
            // values.length > 1
            default:
                Comparable<?> max = values[0];

                for (int index = 1; index < values.length; index++) {
                    Comparable o = values[index];
                    if (o.compareTo(max) > 0) {
                        max = o;
                    }
                }

                return max;
        }
    }
}
