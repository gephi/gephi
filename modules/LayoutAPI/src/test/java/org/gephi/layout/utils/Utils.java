package org.gephi.layout.utils;

import org.gephi.layout.LayoutModelImpl;
import org.gephi.workspace.impl.WorkspaceImpl;

public class Utils {

    public static LayoutModelImpl newLayoutModel() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        LayoutModelImpl model = new LayoutModelImpl(workspace);
        workspace.add(model);
        return model;
    }
}
