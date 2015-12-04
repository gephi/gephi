/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance;

import org.gephi.appearance.api.GraphFunction;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;

/**
 *
 * @author mbastian
 */
public class GraphFunctionImpl extends FunctionImpl implements GraphFunction, RankingFunction, PartitionFunction {

    public GraphFunctionImpl(String id, String name, Class<? extends Element> elementClass, Graph graph, Transformer transformer, TransformerUI transformerUI, RankingImpl ranking, Interpolator interpolator) {
        super(id, name, elementClass, graph, null, transformer, transformerUI, null, ranking, interpolator);
    }

    public GraphFunctionImpl(String id, String name, Class<? extends Element> elementClass, Graph graph, Transformer transformer, TransformerUI transformerUI, PartitionImpl partition) {
        super(id, name, elementClass, graph, null, transformer, transformerUI, partition, null, null);
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
    public PartitionImpl getPartition() {
        return partition;
    }

    @Override
    public RankingImpl getRanking() {
        return ranking;
    }
}
