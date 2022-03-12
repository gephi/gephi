package org.gephi.filters;

import org.gephi.graph.GraphGenerator;
import org.gephi.graph.api.GraphModel;
import org.gephi.workspace.impl.WorkspaceImpl;

public class Utils {

    public static FilterModelImpl newFilterModel() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        FilterModelImpl model = new FilterModelImpl(workspace);
        workspace.add(model);
        return model;
    }

    public static FilterModelImpl newFilterModelWithGraph() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        GraphModel graphModel = GraphGenerator.build().generateTinyGraph().addIntNodeColumn().getGraph().getModel();
        workspace.add(graphModel);

        FilterModelImpl model = new FilterModelImpl(workspace);
        workspace.add(model);
        return model;
    }
}
