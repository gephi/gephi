/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.project.api.Workspace;

/**
 *
 * @author mbastian
 */
public class LegacyMapHelper {

    protected Map<Integer, String> nodeIndexToIds = new HashMap<>();
    protected Map<Integer, String> edgeIndexToIds = new HashMap<>();
    protected Map<Integer, Integer> preToIdMap = new HashMap<>();

    protected static LegacyMapHelper get(Workspace workspace) {
        LegacyMapHelper lh = workspace.getLookup().lookup(LegacyMapHelper.class);
        if (lh == null) {
            lh = new LegacyMapHelper();
            workspace.add(lh);
        }
        return lh;
    }

    protected static GraphModel getGraphModel(Workspace workspace) {
        GraphModel gm = workspace.getLookup().lookup(GraphModel.class);
        if (gm == null) {
            Configuration configuration = new Configuration();
            configuration.setNodeIdType(Integer.class);
            configuration.setEdgeIdType(Integer.class);
            configuration.setTimeRepresentation(TimeRepresentation.INTERVAL);

            gm = GraphModel.Factory.newInstance(configuration);
            workspace.add(gm);
        }
        return gm;
    }
}
