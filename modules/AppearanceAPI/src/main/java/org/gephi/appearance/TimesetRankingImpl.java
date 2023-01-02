package org.gephi.appearance;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.TimeIndex;
import org.gephi.graph.api.types.TimeSet;

public class TimesetRankingImpl extends AttributeRankingImpl {

    public TimesetRankingImpl(Column column) {
        super(column);
    }

    @Override
    public Number getValue(Element element, Graph graph) {
        TimeSet timeSet = (TimeSet) element.getAttribute(column.get(), graph.getView());
        if (timeSet != null) {
            // TODO Make this configurable via Estimator
            return timeSet.getMinDouble();
        }
        return null;
    }

    @Override
    public Number getMinValue(Graph graph) {
        return getIndex(graph).getMinTimestamp();
    }

    @Override
    public Number getMaxValue(Graph graph) {
        return getIndex(graph).getMaxTimestamp();
    }

    private TimeIndex<? extends Element> getIndex(Graph graph) {
        if (this.column.get().getTable().isNodeTable()) {
            return graph.getModel().getNodeTimeIndex(graph.getView());
        } else {
            return graph.getModel().getEdgeTimeIndex(graph.getView());
        }
    }

    @Override
    public boolean isValid(Graph graph) {
        if (column.get() != null) {
            TimeIndex timeIndex = getIndex(graph);
            return timeIndex.getMinTimestamp() != Double.NEGATIVE_INFINITY &&
                timeIndex.getMaxTimestamp() != Double.POSITIVE_INFINITY;
        }
        return false;
    }
}