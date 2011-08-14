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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Attributable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.spi.RankingBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Ranking builder for attributes. Builds the {@link Ranking} instances that
 * maps to all numerical attribute columns.
 * <p>
 * The ranking is built for the workspace associated to the given {@link RankingModel}.
 * <p>
 * When the column is dynamic, the ranking uses the current time interval defined
 * in the DynamicAPI. The time interval value is set when the ranking is built and
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
        Graph graph = graphModel.getGraphVisible();

        //Nodes
        for (AttributeColumn col : attributeModel.getNodeTable().getColumns()) {
            if (attributeUtils.isNumberColumn(col)) {
                AttributeRanking ranking = new AttributeRanking(Ranking.NODE_ELEMENT, col, graph);
                rankings.add(ranking);
            }
        }

        //Edges
        for (AttributeColumn col : attributeModel.getEdgeTable().getColumns()) {
            if (attributeUtils.isNumberColumn(col)) {
                AttributeRanking ranking = new AttributeRanking(Ranking.EDGE_ELEMENT, col, graph);
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
                        DynamicAttributeRanking ranking = new DynamicAttributeRanking(Ranking.NODE_ELEMENT, col, graph, visibleInterval, dynamicModel.getNumberEstimator());
                        rankings.add(ranking);
                    }
                }

                //Edges
                for (AttributeColumn col : attributeModel.getEdgeTable().getColumns()) {
                    if (attributeUtils.isDynamicNumberColumn(col)) {
                        DynamicAttributeRanking ranking = new DynamicAttributeRanking(Ranking.EDGE_ELEMENT, col, graph, visibleInterval, dynamicModel.getNumberEstimator());
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

    private static class AttributeRanking extends AbstractRanking<Attributable> {

        private final AttributeColumn column;
        private final Graph graph;

        public AttributeRanking(String elementType, AttributeColumn column, Graph graph) {
            super(elementType, column.getId());
            this.column = column;
            this.graph = graph;
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
            Graph currentGraph = graphModel.getGraphVisible();
            AttributeRanking newRanking = new AttributeRanking(elementType, column, currentGraph);
            return newRanking;
        }
    }

    private static class DynamicAttributeRanking extends AbstractRanking<Attributable> {

        private final AttributeColumn column;
        private final Graph graph;
        private final TimeInterval timeInterval;
        private final Estimator estimator;

        public DynamicAttributeRanking(String elementType, AttributeColumn column, Graph graph, TimeInterval timeInterval, Estimator estimator) {
            super(elementType, column.getId());
            this.column = column;
            this.timeInterval = timeInterval;
            this.estimator = estimator;
            this.graph = graph;
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
        protected DynamicAttributeRanking clone() {
            TimeInterval visibleInterval = timeInterval;
            Estimator currentEstimator = estimator;
            DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
            if (dynamicController != null) {
                DynamicModel dynamicModel = dynamicController.getModel();
                if (dynamicModel != null) {
                    visibleInterval = dynamicModel.getVisibleInterval();
                    currentEstimator = dynamicModel.getNumberEstimator();
                }
            }
            GraphModel graphModel = graph.getGraphModel();
            Graph currentGraph = graphModel.getGraphVisible();
            DynamicAttributeRanking newRanking = new DynamicAttributeRanking(elementType, column, currentGraph, visibleInterval, currentEstimator);
            return newRanking;
        }
    }
}
