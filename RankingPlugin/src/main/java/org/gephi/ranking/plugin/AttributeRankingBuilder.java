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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.gephi.data.attributes.api.*;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
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
    private final AttributeController attributeController;
    private final AttributeUtils attributeUtils;

    public AttributeRankingBuilder() {
        attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeUtils = Lookup.getDefault().lookup(AttributeUtils.class);
        graphController = Lookup.getDefault().lookup(GraphController.class);
    }

    @Override
    public Ranking[] buildRanking(RankingModel model) {
        AttributeModel attributeModel = attributeController.getModel(model.getWorkspace());
        List<Ranking> rankings = new ArrayList<Ranking>();
        GraphModel graphModel = graphController.getModel(model.getWorkspace());

        //Nodes
        for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
            if (attributeUtils.isNumberColumn(col)) {
                AttributeRanking ranking = new AttributeRanking(Ranking.NODE_ELEMENT, col, graphModel, model);
                rankings.add(ranking);
            }
        }

        //Edges
        for (AttributeColumn col : attributeModel.getEdgeTable().getColumns()) {
            if (attributeUtils.isNumberColumn(col)) {
                AttributeRanking ranking = new AttributeRanking(Ranking.EDGE_ELEMENT, col, graphModel, model);
                rankings.add(ranking);
            }
        }

        //Dynamic
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        if (dynamicController != null) {
            DynamicModel dynamicModel = dynamicController.getModel();
            if (dynamicModel != null) {
                TimeInterval visibleInterval = dynamicModel.getVisibleInterval();

                //Nodes
                for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
                    if (attributeUtils.isDynamicNumberColumn(col)) {
                        DynamicAttributeRanking ranking = new DynamicAttributeRanking(Ranking.NODE_ELEMENT, col, graphModel, model,
                                visibleInterval, dynamicModel.getNumberEstimator());
                        rankings.add(ranking);
                    }
                }

                //Edges
                for (AttributeColumn col : attributeModel.getEdgeTable().getColumns()) {
                    if (attributeUtils.isDynamicNumberColumn(col)) {
                        DynamicAttributeRanking ranking = new DynamicAttributeRanking(Ranking.EDGE_ELEMENT, col, graphModel, model,
                                visibleInterval, dynamicModel.getNumberEstimator());
                        rankings.add(ranking);
                    }
                }
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
        } else if (ranking instanceof DynamicAttributeRanking) {
            return ((DynamicAttributeRanking) ranking).clone();
        } else {
            throw new IllegalArgumentException("Ranking must be an AttributeRanking or DynamicAttributeRanking");
        }
    }

    public static class AttributeRanking extends AbstractRanking<Attributable> {

        private final AttributeColumn column;
        private final Graph graph;

        public AttributeRanking(String elementType, AttributeColumn column, GraphModel graphModel, RankingModel rankingModel) {
            super(elementType, column.getId(), rankingModel);
            this.column = column;
            this.graph = rankingModel.useLocalScale() ? graphModel.getGraphVisible() : graphModel.getGraph();
        }

        @Override
        public Number getValue(Attributable attributable) {
            return (Number) attributable.getAttributes().getValue(column.getIndex());
        }

        @Override
        public float normalize(Number value) {
            return (value.floatValue() - getMinimumValue().floatValue()) / (float) (getMaximumValue().floatValue() - getMinimumValue().floatValue());
        }

        @Override
        public Number unNormalize(float normalizedValue) {
            double val = (normalizedValue * (getMaximumValue().doubleValue() - getMinimumValue().doubleValue())) + getMinimumValue().doubleValue();
            switch (column.getType()) {
                case BIGDECIMAL:
                    return new BigDecimal(val);
                case BIGINTEGER:
                    return new BigInteger("" + val);
                case DOUBLE:
                    return new Double(val);
                case FLOAT:
                    return new Float(val);
                case INT:
                    return new Integer((int) val);
                case LONG:
                    return new Long((long) val);
                case SHORT:
                    return new Short((short) val);
                default:
                    return new Double(val);
            }
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
            GraphModel graphModel = graph.getGraphModel();
            AttributeRanking newRanking = new AttributeRanking(elementType, column, graphModel, rankingModel);
            return newRanking;
        }
    }

    public static class DynamicAttributeRanking extends AbstractRanking<Attributable> {

        private final AttributeColumn column;
        private final Graph graph;
        private final TimeInterval timeInterval;
        private final Estimator estimator;
        private final boolean localScale;

        public DynamicAttributeRanking(String elementType, AttributeColumn column, GraphModel graphModel, RankingModel rankingModel, TimeInterval timeInterval, Estimator estimator) {
            super(elementType, column.getId(), rankingModel);
            this.column = column;
            this.timeInterval = timeInterval;
            this.estimator = estimator;
            this.localScale = rankingModel.useLocalScale();
            this.graph = rankingModel.useLocalScale() ? graphModel.getGraphVisible() : graphModel.getGraph();;
        }

        @Override
        public Number getValue(Attributable attributable) {
            DynamicType<? extends Number> dynamicType = (DynamicType<? extends Number>) attributable.getAttributes().getValue(column.getIndex());
            if (dynamicType != null) {
                return (Number) dynamicType.getValue(timeInterval == null ? Double.NEGATIVE_INFINITY : timeInterval.getLow(),
                        timeInterval == null ? Double.POSITIVE_INFINITY : timeInterval.getHigh(), estimator);
            }
            return null;
        }

        public Number getValue(Attributable attributable, TimeInterval timeInterval, Estimator estimator) {
            DynamicType<? extends Number> dynamicType = (DynamicType<? extends Number>) attributable.getAttributes().getValue(column.getIndex());
            if (dynamicType != null) {
                return (Number) dynamicType.getValue(timeInterval == null ? Double.NEGATIVE_INFINITY : timeInterval.getLow(),
                        timeInterval == null ? Double.POSITIVE_INFINITY : timeInterval.getHigh(), estimator);
            }
            return null;
        }

        @Override
        public float normalize(Number value) {
            return (value.floatValue() - getMinimumValue().floatValue()) / (float) (getMaximumValue().floatValue() - getMinimumValue().floatValue());
        }

        @Override
        public Number unNormalize(float normalizedValue) {
            double val = (normalizedValue * (getMaximumValue().doubleValue() - getMinimumValue().doubleValue())) + getMinimumValue().doubleValue();
            switch (column.getType()) {
                case BIGDECIMAL:
                    return new BigDecimal(val);
                case BIGINTEGER:
                    return new BigInteger("" + val);
                case DOUBLE:
                    return new Double(val);
                case FLOAT:
                    return new Float(val);
                case INT:
                    return new Integer((int) val);
                case LONG:
                    return new Long((long) val);
                case SHORT:
                    return new Short((short) val);
                default:
                    return new Double(val);
            }
        }

        @Override
        public String getDisplayName() {
            return column.getTitle();
        }

        @Override
        public Number getMaximumValue() {
            if (maximum == null) {
                DynamicAttributeRanking.refreshMinMax(this, graph);
            }
            return maximum;
        }

        @Override
        public Number getMinimumValue() {
            if (minimum == null) {
                DynamicAttributeRanking.refreshMinMax(this, graph);
            }
            return minimum;
        }

        @Override
        protected DynamicAttributeRanking clone() {
            TimeInterval visibleInterval = timeInterval;
            Estimator currentEstimator = estimator;
            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
            if (dynamicController != null) {
                DynamicModel dynamicModel = dynamicController.getModel(graph.getGraphModel().getWorkspace());
                if (dynamicModel != null) {
                    visibleInterval = dynamicModel.getVisibleInterval();
                    currentEstimator = dynamicModel.getNumberEstimator();
                }
            }
            DynamicAttributeRanking newRanking = new DynamicAttributeRanking(elementType, column,
                    graph.getGraphModel(), rankingModel, visibleInterval, currentEstimator);
            return newRanking;
        }

        public static void refreshMinMax(DynamicAttributeRanking ranking, Graph graph) {
            boolean localScale = ranking.localScale;
            if (ranking.getElementType().equals(Ranking.NODE_ELEMENT)) {
                List<Comparable> objects = new ArrayList<Comparable>();
                for (Node node : graph.getNodes().toArray()) {
                    Comparable value = (Comparable) ranking.getValue(node, localScale ? ranking.timeInterval : null, Estimator.MIN);
                    if (value != null) {
                        objects.add(value);
                    }
                    value = (Comparable) ranking.getValue(node, localScale ? ranking.timeInterval : null, Estimator.MAX);
                    if (value != null) {
                        objects.add(value);
                    }
                }
                ranking.setMinimumValue((Number) getMin(objects.toArray(new Comparable[0])));
                ranking.setMaximumValue((Number) getMax(objects.toArray(new Comparable[0])));
            } else if (ranking.getElementType().equals(Ranking.EDGE_ELEMENT)) {
                List<Comparable> objects = new ArrayList<Comparable>();
                for (Edge edge : graph.getEdges().toArray()) {
                    Comparable value = (Comparable) ranking.getValue(edge, localScale ? ranking.timeInterval : null, Estimator.MIN);
                    if (value != null) {
                        objects.add(value);
                    }
                    value = (Comparable) ranking.getValue(edge, localScale ? ranking.timeInterval : null, Estimator.MAX);
                    if (value != null) {
                        objects.add(value);
                    }
                }
                ranking.setMinimumValue((Number) getMin(objects.toArray(new Comparable[0])));
                ranking.setMaximumValue((Number) getMax(objects.toArray(new Comparable[0])));
            }
        }
    }
}
