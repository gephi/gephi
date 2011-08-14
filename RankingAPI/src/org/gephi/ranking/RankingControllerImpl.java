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
 * Implementation of the <code>RankingController</code> interface.
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
