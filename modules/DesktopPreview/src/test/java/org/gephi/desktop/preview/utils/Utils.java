package org.gephi.desktop.preview.utils;

import org.gephi.desktop.preview.PreviewUIModelImpl;
import org.gephi.preview.PreviewModelImpl;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.workspace.impl.WorkspaceImpl;
import org.openide.util.Lookup;

public class Utils {

    public static PreviewUIModelImpl newPreviewUIModel() {
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);
        PreviewModel previewModel = previewController.getModel(workspace);
        PreviewUIModelImpl model = new PreviewUIModelImpl(previewModel);
        workspace.add(model);
        return model;
    }
}
