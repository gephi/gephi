package org.gephi.preview.utils;

import org.gephi.preview.PreviewModelImpl;
import org.gephi.project.api.Workspace;
import org.gephi.workspace.impl.WorkspaceImpl;

public class Utils {

    public static PreviewModelImpl newPreviewModel() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        PreviewModelImpl model = new PreviewModelImpl(workspace);
        workspace.add(model);
        return model;
    }

    public static PreviewModelImpl getPreviewModel(Workspace workspace) {
        return workspace.getLookup().lookup(PreviewModelImpl.class);
    }
}
