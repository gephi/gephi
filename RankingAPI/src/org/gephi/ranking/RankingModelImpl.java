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
package org.gephi.ranking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.*;
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
    private final List<AutoRanking> autoRankings;
    private final RankingAutoTransformer autoTransformer;
    private Interpolator interpolator;
    private boolean localScale = false;

    public RankingModelImpl(Workspace workspace) {
        this.workspace = workspace;
        this.listeners = Collections.synchronizedList(new ArrayList<RankingListener>());
        this.autoRankings = Collections.synchronizedList(new ArrayList<AutoRanking>());
        this.interpolator = Interpolator.LINEAR;
        this.autoTransformer = new RankingAutoTransformer(this);
    }

    public void select() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeController.getModel(workspace).addAttributeListener(this);
        if (!autoRankings.isEmpty()) {
            autoTransformer.start();
        }
    }

    public void unselect() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeController.getModel(workspace).removeAttributeListener(this);
        autoTransformer.stop();
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

    public void addAutoRanking(Ranking ranking, Transformer transformer) {
        AutoRanking autoRanking = new AutoRanking(ranking, transformer);
        removeAutoRanking(transformer);
        autoRankings.add(autoRanking);
        autoTransformer.start();
    }

    public void removeAutoRanking(Transformer transformer) {
        for (AutoRanking r : autoRankings.toArray(new AutoRanking[0])) {
            if (r.getTransformer().equals(transformer)) {
                autoRankings.remove(r);
            }
        }
        if (autoRankings.isEmpty()) {
            autoTransformer.stop();
        }
    }

    public Ranking getAutoTransformerRanking(Transformer transformer) {
        for (AutoRanking autoRanking : autoRankings) {
            if (autoRanking.getTransformer().equals(transformer)) {
                return autoRanking.getRanking();
            }
        }
        return null;
    }

    public List<AutoRanking> getAutoRankings() {
        return autoRankings;
    }

    @Override
    public boolean useLocalScale() {
        return localScale;
    }

    public void setLocalScale(boolean localScale) {
        this.localScale = localScale;
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

    public class AutoRanking {

        private final RankingBuilder builder;
        private final Ranking ranking;
        private final Transformer transformer;

        public AutoRanking(Ranking ranking, Transformer transformer) {
            this.ranking = ranking;
            this.transformer = transformer;
            this.builder = getBuilder(ranking);
        }

        public Ranking getRanking() {
            if (builder != null) {
                return builder.refreshRanking(ranking);
            }
            return ranking;
        }

        public Transformer getTransformer() {
            return transformer;
        }

        private RankingBuilder getBuilder(Ranking ranking) {
            Collection<? extends RankingBuilder> builders = Lookup.getDefault().lookupAll(RankingBuilder.class);
            for (RankingBuilder b : builders) {
                Ranking[] builtRankings = b.buildRanking(RankingModelImpl.this);
                if (builtRankings != null) {
                    for (Ranking r : builtRankings) {
                        if (r.getElementType().equals(ranking.getElementType()) && r.getName().equals(ranking.getName())) {
                            return b;
                        }
                    }
                }
            }
            return null;
        }
    }
}
