/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.ranking.api.Interpolator;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.Transformer;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Scheduled thread executor executing the ranking at a fixed delay.
 *
 * @author Mathieu Bastian
 */
public class RankingAutoTransformer implements Runnable, AttributeListener {

    private static final long DEFAULT_DELAY = 500;  //ms
    private ScheduledExecutorService executor;
    private final RankingModelImpl model;
    private final GraphController graphController;
    private final DynamicController dynamicController;
    private final AttributeModel attributeModel;
    private final GraphModel graphModel;
    private final DynamicModel dynamicModel;
    //Verisonning states
    private int lastView = -1;
    private int lastVersion = -1;
    private boolean lastLocalScaleFlag = false;
    private TimeInterval lastTimeInterval = null;
    private boolean valueChanged = false;
    private Interpolator lastInterpolator;

    public RankingAutoTransformer(RankingModelImpl model) {
        this.model = model;
        graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel(model.getWorkspace());
        dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(model.getWorkspace());
        dynamicModel = dynamicController.getModel(model.getWorkspace());
        lastLocalScaleFlag = model.useLocalScale();
    }

    public void start() {
        if (executor == null) {
            //Attribute listening
            attributeModel.addAttributeListener(this);
            lastInterpolator = model.getInterpolator();
            lastLocalScaleFlag = model.useLocalScale();

            executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "Ranking Auto Transformer");
                    return t;
                }
            });
            executor.scheduleWithFixedDelay(this, 0, getDelayInMs(), TimeUnit.MILLISECONDS);
        }
    }

    public void stop() {
        //Attribute stop listening
        attributeModel.removeAttributeListener(this);
        lastVersion = -1;
        lastView = -1;
        lastTimeInterval = null;
        valueChanged = false;
        lastLocalScaleFlag = model.useLocalScale();

        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            executor = null;
        }
    }

    public void run() {
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        int nodeVersion = graph.getNodeVersion();
        int edgeVersion = graph.getEdgeVersion();
        int viewId = graphModel.getVisibleView().getViewId();
        Interpolator interpolator = model.getInterpolator();
        TimeInterval timeInterval = dynamicModel.getVisibleInterval();
        boolean localScale = model.useLocalScale();

        //Test if something changed        
        if (viewId == lastView
                && (nodeVersion + edgeVersion) == lastVersion
                && !valueChanged
                && lastInterpolator.equals(interpolator)
                && ((timeInterval == null && lastTimeInterval == null) || timeInterval.equals(lastTimeInterval))
                && localScale == lastLocalScaleFlag) {
            return;
        }
        lastView = viewId;
        lastVersion = edgeVersion + nodeVersion;
        valueChanged = false;
        lastInterpolator = interpolator;
        lastTimeInterval = timeInterval;
        lastLocalScaleFlag = localScale;
        
        for (RankingModelImpl.AutoRanking autoRanking : model.getAutoRankings()) {

            Ranking ranking = autoRanking.getRanking();
            Transformer transformer = autoRanking.getTransformer();

            if (ranking.getElementType().equals(Ranking.NODE_ELEMENT)) {
                for (Node node : graph.getNodes().toArray()) {
                    Number value = ranking.getValue(node);
                    if (value != null) {
                        float normalizedValue = ranking.normalize(value);
                        if (transformer.isInBounds(normalizedValue)) {
                            normalizedValue = interpolator.interpolate(normalizedValue);
                            transformer.transform(node.getNodeData(), normalizedValue);
                        }
                    }
                }
            } else if (ranking.getElementType().equals(Ranking.EDGE_ELEMENT)) {
                for (Edge edge : graph.getEdgesAndMetaEdges().toArray()) {
                    Number value = ranking.getValue(edge);
                    if (value != null) {
                        float normalizedValue = ranking.normalize(value);
                        if (transformer.isInBounds(normalizedValue)) {
                            normalizedValue = interpolator.interpolate(normalizedValue);
                            transformer.transform(edge.getEdgeData(), normalizedValue);
                        }
                    }
                }
            }
        }
    }

    public void attributesChanged(AttributeEvent event) {
        if (event.getEventType().equals(AttributeEvent.EventType.SET_VALUE)) {
            valueChanged = true;
        }
    }

    private long getDelayInMs() {
        long defaultDelay = NbPreferences.forModule(RankingAutoTransformer.class).getLong("Ranking_Auto_Transformer_Default_Delay", DEFAULT_DELAY);
        return NbPreferences.forModule(RankingAutoTransformer.class).getLong("Ranking_Auto_Transformer_Delay", defaultDelay);
    }
}
