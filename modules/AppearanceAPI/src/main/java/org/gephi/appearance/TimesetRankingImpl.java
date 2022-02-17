package org.gephi.appearance;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeIndex;

public class TimesetRankingImpl extends RankingImpl {

    protected enum Mode {MIN, MAX};

    private final Class elementClass;
    private final Mode mode;

    public TimesetRankingImpl(Class elementClass, Mode mode) {
        super();
        this.elementClass = elementClass;
        this.mode = mode;
    }

    @Override
    public Number getValue(Element element, Graph graph) {
        Interval timeBound = element.getTimeBounds();
        if(timeBound != null) {
            return mode.equals(Mode.MIN) ? timeBound.getLow() : timeBound.getHigh();
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

    private TimeIndex getIndex(Graph graph) {
        return elementClass.isAssignableFrom(Node.class) ? graph.getModel().getNodeTimeIndex(graph.getView()) : graph.getModel().getEdgeTimeIndex(graph.getView());
    }

    @Override
    public boolean isValid(Graph graph) {
        return graph.getModel().isDynamic();
    }


}