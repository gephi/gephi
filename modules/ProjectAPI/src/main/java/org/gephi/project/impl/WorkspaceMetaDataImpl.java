package org.gephi.project.impl;

import org.gephi.project.api.WorkspaceMetaData;

public class WorkspaceMetaDataImpl implements WorkspaceMetaData {

    private String description = "";
    private String title = "";

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description == null ? "" : description;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }
}
