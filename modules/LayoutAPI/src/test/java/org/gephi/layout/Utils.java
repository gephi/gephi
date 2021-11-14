package org.gephi.layout;

import org.gephi.workspace.impl.WorkspaceImpl;

public class Utils {

    public static LayoutModelImpl newLayoutModel() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        LayoutModelImpl model = new LayoutModelImpl(workspace);
        workspace.add(model);
        return model;
    }
}
