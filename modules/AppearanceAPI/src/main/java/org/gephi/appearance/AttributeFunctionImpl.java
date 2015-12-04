/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance;

import org.gephi.appearance.api.AttributeFunction;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author mbastian
 */
public class AttributeFunctionImpl extends FunctionImpl implements RankingFunction, PartitionFunction, AttributeFunction {

    public AttributeFunctionImpl(String id, Graph graph, Column column, Transformer transformer, TransformerUI transformerUI, RankingImpl ranking, Interpolator interpolator) {
        super(id, null, AttributeUtils.isNodeColumn(column) ? Node.class : Edge.class, graph, column, transformer, transformerUI, null, ranking, interpolator);
    }

    public AttributeFunctionImpl(String id, Graph graph, Column column, Transformer transformer, TransformerUI transformerUI, PartitionImpl partition) {
        super(id, null, AttributeUtils.isNodeColumn(column) ? Node.class : Edge.class, graph, column, transformer, transformerUI, partition, null, null);
    }

    @Override
    public Interpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        this.interpolator = interpolator;
    }

    @Override
    public Column getColumn() {
        return column;
    }

    @Override
    public Partition getPartition() {
        return partition;
    }

    @Override
    public Ranking getRanking() {
        return ranking;
    }

    @Override
    public String toString() {
        return column.getTitle();
    }
}
