package org.gephi.filters;

import org.gephi.graph.GraphGenerator;
import org.gephi.project.impl.WorkspaceImpl;

public class Utils {

    public static FilterModelImpl newFilterModel() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        return workspace.getLookup().lookup(FilterModelImpl.class);
    }

    public static FilterModelImpl newFilterModelWithGraph() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        GraphGenerator.build(workspace).generateTinyGraph().addIntNodeColumn();
        return workspace.getLookup().lookup(FilterModelImpl.class);
    }
}
