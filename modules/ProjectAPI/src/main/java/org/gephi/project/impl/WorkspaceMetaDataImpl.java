package org.gephi.project.impl;

import org.gephi.project.api.WorkspaceMetaData;

public class WorkspaceMetaDataImpl implements WorkspaceMetaData {

    private String description = "";

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
