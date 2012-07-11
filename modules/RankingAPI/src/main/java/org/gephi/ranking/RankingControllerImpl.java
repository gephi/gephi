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

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.ranking.api.Interpolator;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.RankingEvent;
import org.gephi.ranking.api.RankingModel;
import org.gephi.ranking.api.Transformer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the
 * <code>RankingController</code> interface.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = RankingController.class)
public class RankingControllerImpl implements RankingController {
    
    private final GraphController graphController;
    private RankingModelImpl model;
    
    public RankingControllerImpl() {
        graphController = Lookup.getDefault().lookup(GraphController.class);

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {
            
            public void initialize(Workspace workspace) {
            }
            
            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(RankingModelImpl.class);
                if (model == null) {
                    model = new RankingModelImpl(workspace);
                    workspace.add(model);
                }
                model.select();
            }
            
            public void unselect(Workspace workspace) {
                model.unselect();
                model = null;
            }
            
            public void close(Workspace workspace) {
            }
            
            public void disable() {
                model = null;
            }
        });
        
        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(RankingModelImpl.class);
            if (model == null) {
                model = new RankingModelImpl(pc.getCurrentWorkspace());
                pc.getCurrentWorkspace().add(model);
            }
        }
    }
    
    public RankingModel getModel() {
        return model;
    }
    
    public RankingModel getModel(Workspace workspace) {
        RankingModel m = workspace.getLookup().lookup(RankingModelImpl.class);
        if (m == null) {
            m = new RankingModelImpl(workspace);
            workspace.add(m);
        }
        return m;
    }
    
    public void setInterpolator(Interpolator interpolator) {
        if (model != null) {
            model.setInterpolator(interpolator);
        }
    }
    
    public void setUseLocalScale(boolean useLocalScale) {
        if (model != null) {
            model.setLocalScale(useLocalScale);
        }
    }
    
    public void transform(Ranking ranking, Transformer transformer) {
        //Refresh ranking
        ranking = model.getRanking(ranking.getElementType(), ranking.getName());
        
        Workspace workspace = model.getWorkspace();
        GraphModel graphModel = graphController.getModel(workspace);
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        Interpolator interpolator = model.getInterpolator();
        
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

        //Send Event
        model.fireRankingListener(new RankingEventImpl(RankingEvent.EventType.APPLY_TRANSFORMER, model, ranking, transformer));
    }
    
    public void startAutoTransform(Ranking ranking, Transformer transformer) {
        model.addAutoRanking(ranking, transformer);

        //Send Event
        model.fireRankingListener(new RankingEventImpl(RankingEvent.EventType.START_AUTO_TRANSFORM, model, ranking, transformer));
    }
    
    public void stopAutoTransform(Transformer transformer) {
        Ranking ranking = model.getAutoTransformerRanking(transformer);
        model.removeAutoRanking(transformer);

        //Send Event
        model.fireRankingListener(new RankingEventImpl(RankingEvent.EventType.STOP_AUTO_TRANSFORM, model, ranking, transformer));
    }
}
