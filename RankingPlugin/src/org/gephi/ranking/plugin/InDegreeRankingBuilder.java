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

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.spi.RankingBuilder;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ranking builder for graph in degree. Builds the {@link Ranking} instances that
 * performs the ranking for node in degrees. 
 * <p>
 * The ranking is built for the workspace associated to the given {@link RankingModel}.
 * 
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RankingBuilder.class, position = 200)
public class InDegreeRankingBuilder implements RankingBuilder {

    private final GraphController graphController;

    public InDegreeRankingBuilder() {
        graphController = Lookup.getDefault().lookup(GraphController.class);
    }

    @Override
    public Ranking[] buildRanking(RankingModel model) {
        Workspace workspace = model.getWorkspace();
        GraphModel graphModel = graphController.getModel(workspace);
        Graph graph = graphModel.getGraphVisible();
        if (graph instanceof DirectedGraph) {
            return new Ranking[]{new InDegreeRanking(Ranking.NODE_ELEMENT, (DirectedGraph) graph)};
        }

        return null;
    }

    @Override
    public Ranking refreshRanking(Ranking ranking) {
        if (ranking == null) {
            throw new NullPointerException();
        }
        if (ranking instanceof InDegreeRanking) {
            return ((InDegreeRanking) ranking).clone();
        } else {
            throw new IllegalArgumentException("Ranking must be an DegreeRanking");
        }
    }

    private static class InDegreeRanking extends AbstractRanking<Node> {

        private final DirectedGraph graph;

        public InDegreeRanking(String elementType, DirectedGraph graph) {
            super(elementType, Ranking.INDEGREE_RANKING);
            this.graph = graph;
        }

        @Override
        public Integer getValue(Node element) {
            return graph.getInDegree(element);
        }

        @Override
        public float normalize(Number value) {
            return (float) ((value.intValue() - getMinimumValue().intValue()) / (float) (getMaximumValue().intValue() - getMinimumValue().intValue()));
        }

        @Override
        public Integer unNormalize(float normalizedValue) {
            return (int) (normalizedValue * (getMaximumValue().intValue() - getMinimumValue().intValue())) + getMinimumValue().intValue();
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(InDegreeRankingBuilder.class, "InDegreeRanking.name");
        }

        @Override
        public Number getMaximumValue() {
            if (maximum == null) {
                AbstractRanking.refreshMinMax(this, graph);
            }
            return maximum;
        }

        @Override
        public Number getMinimumValue() {
            if (minimum == null) {
                AbstractRanking.refreshMinMax(this, graph);
            }
            return minimum;
        }

        @Override
        protected InDegreeRanking clone() {
            GraphModel graphModel = graph.getGraphModel();
            DirectedGraph currentGraph = graphModel.getDirectedGraphVisible();
            return new InDegreeRanking(elementType, currentGraph);
        }
    }
}
