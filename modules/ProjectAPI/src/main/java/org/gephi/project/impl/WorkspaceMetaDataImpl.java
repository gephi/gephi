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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkspaceMetaDataImpl that = (WorkspaceMetaDataImpl) o;

        if (!getDescription().equals(that.getDescription())) {
            return false;
        }
        return getTitle().equals(that.getTitle());
    }

    @Override
    public int hashCode() {
        int result = getDescription().hashCode();
        result = 31 * result + getTitle().hashCode();
        return result;
    }
}
