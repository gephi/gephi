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
package org.gephi.ranking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.gephi.ranking.api.Interpolator;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.api.Ranking;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.RankingEvent;
import org.gephi.ranking.api.RankingListener;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.spi.RankingBuilder;
import org.gephi.ranking.spi.TransformerBuilder;
import org.openide.util.Lookup;

/**
 * Implementation of the <code>RankingModel</code> interface.
 * 
 * @author Mathieu Bastian
 */
public class RankingModelImpl implements RankingModel, AttributeListener {

    private final Workspace workspace;
    private final List<RankingListener> listeners;
    private Interpolator interpolator;

    public RankingModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.listeners = Collections.synchronizedList(new ArrayList<RankingListener>());
        this.interpolator = Interpolator.LINEAR;
    }

    public void select() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeController.getModel(workspace).addAttributeListener(this);
    }

    public void unselect() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeController.getModel(workspace).removeAttributeListener(this);
    }
    private Timer refreshTimer; //hack

    @Override
    public void attributesChanged(AttributeEvent event) {
        if (event.getEventType().equals(AttributeEvent.EventType.ADD_COLUMN) || event.getEventType().equals(AttributeEvent.EventType.REMOVE_COLUMN)) {
            if (refreshTimer != null) {
                refreshTimer.restart();
            } else {
                refreshTimer = new Timer(500, new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RankingEvent rankingEvent = new RankingEventImpl(RankingEvent.EventType.REFRESH_RANKING, RankingModelImpl.this);
                        fireRankingListener(rankingEvent);
                    }
                });
                refreshTimer.setRepeats(false);
                refreshTimer.start();
            }
        }
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public Ranking[] getNodeRankings() {
        return getRankings(Ranking.NODE_ELEMENT);
    }

    public Ranking[] getEdgeRankings() {
        return getRankings(Ranking.EDGE_ELEMENT);
    }

    public Ranking[] getRankings(String elementType) {
        List<Ranking> rankings = new ArrayList<Ranking>();
        Collection<? extends RankingBuilder> builders = Lookup.getDefault().lookupAll(RankingBuilder.class);
        for (RankingBuilder builder : builders) {
            Ranking[] builtRankings = builder.buildRanking(this);
            if (builtRankings != null) {
                for (Ranking r : builtRankings) {
                    if (r.getElementType().equals(elementType)) {
                        rankings.add(r);
                    }
                }
            }
        }
        return rankings.toArray(new Ranking[0]);
    }

    public Ranking getRanking(String elementType, String name) {
        Ranking[] rankings = getRankings(elementType);
        for (Ranking r : rankings) {
            if (r.getName().equals(name)) {
                return r;
            }
        }
        return null;
    }

    public Transformer getTransformer(String elementType, String name) {
        for (TransformerBuilder builder : Lookup.getDefault().lookupAll(TransformerBuilder.class)) {
            if (builder.isTransformerForElement(elementType) && builder.getName().equals(name)) {
                return builder.buildTransformer();
            }
        }
        return null;
    }

    public Transformer[] getTransformers(String elementType) {
        List<Transformer> transformers = new ArrayList<Transformer>();
        for (TransformerBuilder builder : Lookup.getDefault().lookupAll(TransformerBuilder.class)) {
            if (builder.isTransformerForElement(elementType)) {
                transformers.add(builder.buildTransformer());
            }
        }
        return transformers.toArray(new Transformer[0]);
    }

    public Interpolator getInterpolator() {
        return interpolator;
    }

    public void setInterpolator(Interpolator interpolator) {
        if (interpolator == null) {
            throw new NullPointerException();
        }
        this.interpolator = interpolator;
    }

    public void addRankingListener(RankingListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeRankingListener(RankingListener listener) {
        listeners.remove(listener);
    }

    public void fireRankingListener(RankingEvent rankingEvent) {
        for (RankingListener listener : listeners) {
            listener.rankingChanged(rankingEvent);
        }
    }
}
