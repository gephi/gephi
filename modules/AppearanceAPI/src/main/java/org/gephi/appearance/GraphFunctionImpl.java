/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.appearance;

import org.gephi.appearance.api.GraphFunction;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Graph;

/**
 *
 * @author mbastian
 */
public class GraphFunctionImpl extends FunctionImpl implements GraphFunction, RankingFunction, PartitionFunction {

    public GraphFunctionImpl(String id, String name, Graph graph, Transformer transformer, TransformerUI transformerUI, RankingImpl ranking) {
        super(id, name, graph, null, transformer, transformerUI, null, ranking);
    }

    public GraphFunctionImpl(String id, String name, Graph graph, Transformer transformer, TransformerUI transformerUI, PartitionImpl partition) {
        super(id, name, graph, null, transformer, transformerUI, partition, null);
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
