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
import org.gephi.graph.api.Column;

/**
 * @author mbastian
 */
public class AttributeFunctionImpl extends FunctionImpl
    implements RankingFunction, PartitionFunction, AttributeFunction {

    public AttributeFunctionImpl(AppearanceModelImpl model, String name, Column column, Transformer transformer,
                                 TransformerUI transformerUI, RankingImpl ranking) {
        super(model, name, column.getTable().getElementClass(), column, transformer,
            transformerUI, null, ranking);
    }

    public AttributeFunctionImpl(AppearanceModelImpl model, String name, Column column, Transformer transformer,
                                 TransformerUI transformerUI, PartitionImpl partition) {
        super(model, name, column.getTable().getElementClass(), column, transformer,
            transformerUI, partition, null);
    }

    @Override
    public Interpolator getInterpolator() {
        return ranking.getInterpolator();
    }

    @Override
    public void setInterpolator(Interpolator interpolator) {
        ranking.setInterpolator(interpolator);
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
