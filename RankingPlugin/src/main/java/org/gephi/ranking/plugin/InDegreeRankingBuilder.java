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

import org.gephi.graph.api.DirectedGraph;
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
        if (graphModel.isDirected()) {
            return new Ranking[]{new InDegreeRanking(Ranking.NODE_ELEMENT, graphModel, model)};
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

        public InDegreeRanking(String elementType, GraphModel graphModel, RankingModel rankingModel) {
            super(elementType, Ranking.INDEGREE_RANKING, rankingModel);
            this.graph = rankingModel.useLocalScale() ? graphModel.getDirectedGraphVisible() : graphModel.getDirectedGraph();;
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
            return new InDegreeRanking(elementType, graphModel, rankingModel);
        }
    }
}
