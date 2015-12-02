/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance;

import org.gephi.appearance.api.Interpolator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Index;

/**
 *
 * @author mbastian
 */
public class AttributeRankingImpl extends RankingImpl {

    protected final Index index;
    protected final Column column;

    public AttributeRankingImpl(Column column, Index index) {
        super();
        this.column = column;
        this.index = index;
    }

    @Override
    protected void refresh() {
        min = index.getMinValue(column);
        max = index.getMaxValue(column);
    }

    @Override
    public Number getValue(Element element) {
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
