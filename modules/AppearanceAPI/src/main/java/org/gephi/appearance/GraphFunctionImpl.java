/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.appearance;

import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.GraphFunction;
import org.gephi.appearance.api.Interpolator;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.spi.Transformer;
import org.gephi.appearance.spi.TransformerUI;
import org.gephi.graph.api.Element;

/**
 * @author mbastian
 */
public class GraphFunctionImpl extends FunctionImpl implements GraphFunction, RankingFunction, PartitionFunction {

    private final String displayName;
    private final AppearanceModel.GraphFunction graphFunction;

    public GraphFunctionImpl(AppearanceModelImpl model, AppearanceModel.GraphFunction graphFunction, String name,
                             String displayName, Class<? extends Element> elementClass,
                             Transformer transformer, TransformerUI transformerUI, RankingImpl ranking) {
        super(model, name, elementClass, null, transformer, transformerUI, null, ranking);
        this.displayName = displayName;
        this.graphFunction = graphFunction;
    }

    public GraphFunctionImpl(AppearanceModelImpl model, AppearanceModel.GraphFunction graphFunction, String name,
                             String displayName, Class<? extends Element> elementClass,
                             Transformer transformer, TransformerUI transformerUI, PartitionImpl partition) {
        super(model, name, elementClass, null, transformer, transformerUI, partition, null);
        this.displayName = displayName;
        this.graphFunction = graphFunction;
    }

    @Override
    public AppearanceModel.GraphFunction getGraphFunction() {
        return graphFunction;
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
    public PartitionImpl getPartition() {
        return partition;
    }

    @Override
    public RankingImpl getRanking() {
        return ranking;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
