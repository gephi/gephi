package org.gephi.desktop.preview.utils;

import org.gephi.desktop.preview.PreviewUIModelImpl;
import org.gephi.project.impl.WorkspaceImpl;

public class Utils {

    public static PreviewUIModelImpl newPreviewUIModel() {
        // WorkspaceImpl.initModels() auto-creates both PreviewModelImpl and PreviewUIModelImpl
        // via the registered Controller SPI implementations.
        WorkspaceImpl workspace = new WorkspaceImpl(null, 0);
        return workspace.getLookup().lookup(PreviewUIModelImpl.class);
    }
}
