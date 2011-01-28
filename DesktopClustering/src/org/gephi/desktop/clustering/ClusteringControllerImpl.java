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
package org.gephi.desktop.clustering;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.clustering.api.Cluster;
import org.gephi.clustering.api.ClusteringController;
import org.gephi.clustering.api.ClusteringModel;
import org.gephi.clustering.spi.Clusterer;
import org.gephi.clustering.spi.ClustererBuilder;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.visualization.VizController;
import org.gephi.visualization.api.selection.SelectionManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ClusteringController.class)
public class ClusteringControllerImpl implements ClusteringController {

    private LongTaskExecutor executor;
    private LongTaskErrorHandler errorHandler;

    public ClusteringControllerImpl() {
        executor = new LongTaskExecutor(true, "Clusterer", 10);
        errorHandler = new LongTaskErrorHandler() {

            public void fatalError(Throwable t) {
                Logger.getLogger("").log(Level.SEVERE, "", t.getCause() != null ? t.getCause() : t);
            }
        };
        executor.setDefaultErrorHandler(errorHandler);
    }

    public void clusterize(final Clusterer clusterer) {
        //Get Graph
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        final GraphModel graphModel = gc.getModel();

        //Model
        final ClusteringModel model = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(ClusteringModel.class);

        //LongTask
        LongTask task = null;
        if (clusterer instanceof LongTask) {
            task = (LongTask) clusterer;
        }
        executor.execute(task, new Runnable() {

            public void run() {
                model.setRunning(true);
                clusterer.execute(graphModel);
                writeColumns(clusterer);
                model.setRunning(false);
            }
        });
    }

    public void cancelClusterize(Clusterer clusterer) {
        executor.cancel();
    }

    private void writeColumns(Clusterer clusterer) {
        Cluster[] clusters = clusterer.getClusters();
        if (clusters != null && clusters.length > 0) {
            ClustererBuilder builder = getBuilder(clusterer);
            AttributeModel am = Lookup.getDefault().lookup(AttributeController.class).getModel();
            String id = "clustering_" + builder.getName();
            String title = "Clustering (" + builder.getName() + ")";
            AttributeColumn col = am.getNodeTable().getColumn(id);
            if (col == null) {
                col = am.getNodeTable().addColumn(id, title, AttributeType.INT, AttributeOrigin.COMPUTED, null);
                StatusDisplayer.getDefault().setStatusText("A new column \"" + title + "\" has been created");
            }
            for (int i = 0; i < clusters.length; i++) {
                Integer clusterId = new Integer(i);
                for (Node n : clusters[i].getNodes()) {
                    n.getNodeData().getAttributes().setValue(col.getIndex(), clusterId);
                }
            }
        }
    }

    public void selectCluster(Cluster cluster) {
        SelectionManager selectionManager = VizController.getInstance().getSelectionManager();
        selectionManager.resetSelection();
        selectionManager.selectNodes(cluster.getNodes());
    }

    public void groupCluster(Cluster cluster) {
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
        if (gm != null) {
            HierarchicalGraph graph = gm.getHierarchicalGraphVisible();
            Node[] newGroup = cluster.getNodes();
            float centroidX = 0;
            float centroidY = 0;
            int len = 0;
            Node group = graph.groupNodes(newGroup);
            cluster.setMetaNode(group);

            group.getNodeData().setLabel("Group");
            group.getNodeData().setSize(10f);
            for (Node child : newGroup) {
                centroidX += child.getNodeData().x();
                centroidY += child.getNodeData().y();
                len++;
            }
            centroidX /= len;
            centroidY /= len;
            group.getNodeData().setX(centroidX);
            group.getNodeData().setY(centroidY);
        }
    }

    public void ungroupCluster(Cluster cluster) {
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel();
        if (gm != null) {
            HierarchicalGraph graph = gm.getHierarchicalGraphVisible();
            graph.ungroupNodes(cluster.getMetaNode());
            cluster.setMetaNode(null);
        }
    }

    public boolean canGroup(Cluster cluster) {
        return cluster.getMetaNode() == null;
    }

    public boolean canUngroup(Cluster cluster) {
        return cluster.getMetaNode() != null;
    }

    private ClustererBuilder getBuilder(Clusterer clusterer) {
        for (ClustererBuilder b : Lookup.getDefault().lookupAll(ClustererBuilder.class)) {
            if (b.getClustererClass().equals(clusterer.getClass())) {
                return b;
            }
        }
        return null;
    }
}
