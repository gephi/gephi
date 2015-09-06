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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.gephi.graph.api.AttributeModel;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.attribute.time.TimestampValueSet;
import org.gephi.graph.api.*;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.spi.RankingBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ranking builder for attributes. Builds the {@link Ranking} instances that
 * maps to all numerical attribute columns. <p> The ranking is built for the
 * workspace associated to the given {@link RankingModel}. <p> When the column
 * is dynamic, the ranking uses the current time interval defined in the
 * DynamicAPI. The time interval value is set when the ranking is built and
 * won't be updated.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RankingBuilder.class)
public class AttributeRankingBuilder implements RankingBuilder {

    private final GraphController graphController;

    public AttributeRankingBuilder() {
        graphController = Lookup.getDefault().lookup(GraphController.class);
    }

    @Override
    public Ranking[] buildRanking(RankingModel model) {
        AttributeModel attributeModel = graphController.getAttributeModel(model.getWorkspace());
        List<Ranking> rankings = new ArrayList<Ranking>();
        GraphModel graphModel = graphController.getGraphModel(model.getWorkspace());

        //Nodes
        for (Column col : attributeModel.getNodeTable()) {
            if (!col.isProperty() && col.isNumber()) {
                AttributeRanking ranking = new AttributeRanking(Ranking.NODE_ELEMENT, col, graphModel, model);
                rankings.add(ranking);
            }
        }

        //Edges
        for (Column col : attributeModel.getEdgeTable()) {
            if (!col.isProperty() && col.isNumber()) {
                AttributeRanking ranking = new AttributeRanking(Ranking.EDGE_ELEMENT, col, graphModel, model);
                rankings.add(ranking);
            }
        }

        //Sort attributes by alphabetical order
        Ranking[] rankingArray = rankings.toArray(new Ranking[0]);
        Arrays.sort(rankingArray, new Comparator<Ranking>() {
            @Override
            public int compare(Ranking a, Ranking b) {
                return (a.toString().compareTo(b.toString()));
            }
        });

        return rankingArray;
    }

    @Override
    public Ranking refreshRanking(Ranking ranking) {
        if (ranking == null) {
            throw new NullPointerException();
        }
        if (ranking instanceof AttributeRanking) {
            return ((AttributeRanking) ranking).clone();
        } else {
            throw new IllegalArgumentException("Ranking must be an AttributeRanking");
        }
    }

    public static class AttributeRanking extends AbstractRanking<Element> {

        private final Column column;
        private final Graph graph;

        public AttributeRanking(String elementType, Column column, GraphModel graphModel, RankingModel rankingModel) {
            super(elementType, column.getId(), rankingModel);
            this.column = column;
            this.graph = rankingModel.useLocalScale() ? graphModel.getGraphVisible() : graphModel.getGraph();
        }

        @Override
        public Number getValue(Element attributable) {
            return (Number) attributable.getAttribute(column, graph.getView());
        }

        @Override
        public float normalize(Number value) {
            return (value.floatValue() - getMinimumValue().floatValue()) / (float) (getMaximumValue().floatValue() - getMinimumValue().floatValue());
        }

        @Override
        public Number unNormalize(float normalizedValue) {
            double val = (normalizedValue * (getMaximumValue().doubleValue() - getMinimumValue().doubleValue())) + getMinimumValue().doubleValue();
            Class type = column.getTypeClass();
            if (column.isDynamic()) {
                type = AttributeUtils.getStaticType((Class<? extends TimestampValueSet>) type);
            }
            if (type.equals(Double.class)) {
                return new Double(val);
            } else if (type.equals(Integer.class)) {
                return new Integer((int) val);
            } else if (type.equals(Float.class)) {
                return new Float(val);
            } else if (type.equals(Long.class)) {
                return new Long((long) val);
            } else if (type.equals(Short.class)) {
                return new Short((short) val);
            } else if (type.equals(Byte.class)) {
                return new Byte((byte) val);
            }
            return new Double(val);
        }

        @Override
        public String getDisplayName() {
            return column.getTitle();
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
        protected AttributeRanking clone() {
            GraphModel graphModel = graph.getView().getGraphModel();
            AttributeRanking newRanking = new AttributeRanking(elementType, column, graphModel, rankingModel);
            return newRanking;
        }
    }
}
