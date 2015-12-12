/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author mbastian
 */
public class AttributeRankingImpl extends RankingImpl {

    protected final Index index;
    protected final Column column;
    protected final Graph graph;

    public AttributeRankingImpl(Column column, Index index) {
        super();
        this.column = column;
        this.index = index;
        this.graph = null;
    }

    public AttributeRankingImpl(Column column, Graph graph) {
        super();
        this.column = column;
        this.graph = graph;
        this.index = null;
    }

    @Override
    protected void refresh() {
        if (index != null) {
            min = index.getMinValue(column);
            max = index.getMaxValue(column);
        } else {
            ElementIterable<? extends Element> iterable = AttributeUtils.isNodeColumn(column) ? graph.getNodes() : graph.getEdges();
            double minN = Double.POSITIVE_INFINITY;
            double maxN = Double.NEGATIVE_INFINITY;
            for (Element el : iterable) {
                if (column.isDynamic()) {
                    TimeMap timeMap = (TimeMap) el.getAttribute(column);
                    if (timeMap != null) {
                        Number numMin = (Number) timeMap.get(graph.getView().getTimeInterval(), Estimator.MIN);
                        Number numMax = (Number) timeMap.get(graph.getView().getTimeInterval(), Estimator.MAX);
                        if (numMin.doubleValue() < minN) {
                            min = numMin;
                        }
                        if (numMax.doubleValue() > maxN) {
                            max = numMax;
                        }
                    }
                } else {
                    Number num = (Number) el.getAttribute(column);
                    if (num.doubleValue() < minN) {
                        min = num;
                    }
                    if (num.doubleValue() > maxN) {
                        max = num;
                    }
                }
            }
        }
    }

    @Override
    public Number getValue(Element element, Graph gr) {
        if (graph != null) {
            return (Number) element.getAttribute(column, gr.getView());
        }
        return (Number) element.getAttribute(column);
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
        if (this.column != other.column && (this.column == null || !this.column.equals(other.column))) {
            return false;
        }
        return true;
    }
}
