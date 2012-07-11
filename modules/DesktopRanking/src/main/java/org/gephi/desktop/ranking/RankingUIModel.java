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
package org.gephi.desktop.ranking;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphView;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.RankingEvent;
import org.gephi.ranking.api.RankingListener;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.AttributeRankingBuilder;
import org.gephi.ranking.spi.TransformerBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class RankingUIModel implements RankingListener, GraphListener {
    //Const

    public static final String CURRENT_TRANSFORMER = "currentTransformer";
    public static final String CURRENT_RANKING = "currentRanking";
    public static final String LIST_VISIBLE = "listVisible";
    public static final String BARCHART_VISIBLE = "barChartVisible";
    public static final String CURRENT_ELEMENT_TYPE = "currentElementType";
    public static final String RANKINGS = "rankings";
    public static final String APPLY_TRANSFORMER = "applyTransformer";
    public static final String START_AUTO_TRANSFORMER = "startAutoTransformer";
    public static final String STOP_AUTO_TRANSFORMER = "stopAutoTransformer";
    public static final String LOCAL_SCALE = "localScale";
    public static final String LOCAL_SCALE_ENABLED = "localScaleEnabled";
    //Current model
    protected final Map<String, LinkedHashMap<String, Transformer>> transformers;
    protected String currentElementType;
    protected final Map<String, Ranking> currentRanking;
    protected final Map<String, Transformer> currentTransformer;
    protected boolean barChartVisible;
    protected boolean listVisible;
    protected boolean localScaleEnabled;
    //Architecture
    private final List<PropertyChangeListener> listeners;
    private RankingUIController controller;
    private final RankingModel model;

    public RankingUIModel(RankingUIController rankingUIController, RankingModel rankingModel) {
        transformers = new HashMap<String, LinkedHashMap<String, Transformer>>();
        listeners = new ArrayList<PropertyChangeListener>();
        currentRanking = new HashMap<String, Ranking>();
        currentTransformer = new HashMap<String, Transformer>();
        model = rankingModel;
        controller = rankingUIController;
        currentElementType = Ranking.NODE_ELEMENT;
        listVisible = false;
        localScaleEnabled = !Lookup.getDefault().lookup(GraphController.class).getModel(rankingModel.getWorkspace()).getVisibleView().isMainView();

        initTransformers();

        //Set default transformer - the first
        for (String elementType : controller.getElementTypes()) {
            currentTransformer.put(elementType, getTransformers(elementType)[0]);
        }

        model.addRankingListener(this);
    }

    public void rankingChanged(RankingEvent event) {
        if (event.is(RankingEvent.EventType.REFRESH_RANKING)) {
            firePropertyChangeEvent(RANKINGS, null, null);
        } else if (event.is(RankingEvent.EventType.APPLY_TRANSFORMER)) {
            firePropertyChangeEvent(APPLY_TRANSFORMER, null, null);
        }
    }

    public void graphChanged(GraphEvent event) {
        if (event.getEventType().equals(GraphEvent.EventType.VISIBLE_VIEW)) {
            boolean localScale = shouldLocalScaleEnabled(event.getSource());
            setLocalScaleEnabled(localScale);
        }
    }

    private boolean shouldLocalScaleEnabled(GraphView view) {
        boolean filteredView = view != null && !view.isMainView();
        if (!filteredView) {
            //Try to see if dynamic ranking
            Ranking r = currentRanking.get(currentElementType);
            if (r != null && r instanceof AttributeRankingBuilder.DynamicAttributeRanking) {
                filteredView = true;
            }
        }
        return filteredView;
    }

    private void setLocalScaleEnabled(boolean enabled) {
        if (enabled != localScaleEnabled) {
            boolean oldValue = localScaleEnabled;
            localScaleEnabled = enabled;
            if (!enabled) {
                setLocalScale(false);
            }
            firePropertyChangeEvent(LOCAL_SCALE_ENABLED, oldValue, enabled);
        }
    }

    public void setCurrentTransformer(Transformer transformer) {
        if (currentTransformer.get(currentElementType) == transformer) {
            return;
        }
        Transformer oldValue = currentTransformer.get(currentElementType);
        currentTransformer.put(currentElementType, transformer);
        if (model.getAutoTransformerRanking(transformer) != null) {
            setCurrentRanking(model.getAutoTransformerRanking(transformer));
        }
        firePropertyChangeEvent(CURRENT_TRANSFORMER, oldValue, transformer);
    }

    public void setCurrentRanking(Ranking ranking) {
        if ((currentRanking.get(currentElementType) == null && ranking == null)
                || (currentRanking.get(currentElementType) != null && currentRanking.get(currentElementType) == ranking)) {
            return;
        }
        Ranking oldValue = currentRanking.get(currentElementType);
        currentRanking.put(currentElementType, ranking);
        firePropertyChangeEvent(CURRENT_RANKING, oldValue, ranking);

        //If selected ranking is dynamic we might want to enable local scale
        setLocalScaleEnabled(shouldLocalScaleEnabled(null));
    }

    public void setListVisible(boolean listVisible) {
        if (this.listVisible == listVisible) {
            return;
        }
        boolean oldValue = this.listVisible;
        this.listVisible = listVisible;
        firePropertyChangeEvent(LIST_VISIBLE, oldValue, listVisible);
    }

    public void setBarChartVisible(boolean barChartVisible) {
        if (this.barChartVisible == barChartVisible) {
            return;
        }
        boolean oldValue = this.barChartVisible;
        this.barChartVisible = barChartVisible;
        firePropertyChangeEvent(BARCHART_VISIBLE, oldValue, barChartVisible);
    }

    public void setLocalScale(boolean localScale) {
        if (model.useLocalScale() == localScale) {
            return;
        }
        boolean oldValue = model.useLocalScale();
        Lookup.getDefault().lookup(RankingController.class).setUseLocalScale(localScale);
        firePropertyChangeEvent(LOCAL_SCALE, oldValue, localScale);
    }

    public void setCurrentElementType(String elementType) {
        if (this.currentElementType.equals(elementType)) {
            return;
        }
        String oldValue = this.currentElementType;
        this.currentElementType = elementType;
        firePropertyChangeEvent(CURRENT_ELEMENT_TYPE, oldValue, elementType);
    }

    public Ranking getCurrentRanking() {
        return currentRanking.get(currentElementType);
    }

    public Transformer getCurrentTransformer() {
        return currentTransformer.get(currentElementType);
    }

    public Transformer getCurrentTransformer(String elementType) {
        return currentTransformer.get(elementType);
    }

    public String getCurrentElementType() {
        return currentElementType;
    }

    public boolean isBarChartVisible() {
        return barChartVisible;
    }

    public boolean isLocalScale() {
        return model.useLocalScale();
    }

    public boolean isLocalScaleEnabled() {
        return localScaleEnabled;
    }

    public boolean isListVisible() {
        return listVisible;
    }

    public Ranking[] getRankings(String elmType) {
        Ranking[] rankings = model.getRankings(elmType);
        Ranking current = getCurrentRanking();
        if (current != null) {
            //Update selectedRanking with latest version
            for (Ranking r : rankings) {
                if (r.getName().equals(current.getName())) {
                    currentRanking.put(elmType, r);
                    break;
                }
            }
        }
        return rankings;
    }

    public Ranking[] getRankings() {
        return getRankings(currentElementType);
    }

    public boolean isAutoTransformer(Transformer transformer) {
        return model.getAutoTransformerRanking(transformer) != null;
    }

    public void setAutoTransformer(Transformer transformer, boolean enable) {
        RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
        if (enable) {
            rankingController.startAutoTransform(getCurrentRanking(), transformer);
            firePropertyChangeEvent(START_AUTO_TRANSFORMER, null, transformer);
        } else {
            rankingController.stopAutoTransform(transformer);
            firePropertyChangeEvent(STOP_AUTO_TRANSFORMER, null, transformer);
        }
    }

    private void initTransformers() {
        for (String elementType : controller.getElementTypes()) {
            LinkedHashMap<String, Transformer> elmtTransformers = new LinkedHashMap<String, Transformer>();
            transformers.put(elementType, elmtTransformers);
        }

        for (TransformerBuilder builder : Lookup.getDefault().lookupAll(TransformerBuilder.class)) {
            for (String elementType : controller.getElementTypes()) {
                Map<String, Transformer> elmtTransformers = transformers.get(elementType);
                if (builder.isTransformerForElement(elementType)) {
                    elmtTransformers.put(builder.getName(), builder.buildTransformer());
                }
            }
        }
    }

    public Transformer[] getTransformers(String elementType) {
        return transformers.get(elementType).values().toArray(new Transformer[0]);
    }

    public Transformer[] getTransformers() {
        return getTransformers(currentElementType);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private void firePropertyChangeEvent(String propertyName, Object beforeValue, Object afterValue) {
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, beforeValue, afterValue);
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(event);
        }
    }
}
