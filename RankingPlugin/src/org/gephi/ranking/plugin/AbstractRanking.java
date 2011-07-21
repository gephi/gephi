/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ranking.plugin;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.Ranking;

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

    private final String name;
    private final String elementType;
    protected Number minimum;
    protected Number maximum;

    public AbstractRanking(String elementType, String name) {
        this.elementType = elementType;
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
