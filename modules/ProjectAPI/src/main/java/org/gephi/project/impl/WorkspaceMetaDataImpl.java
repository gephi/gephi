package org.gephi.project.impl;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkspaceMetaDataImpl that = (WorkspaceMetaDataImpl) o;

        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return description != null ? description.hashCode() : 0;
    }
}
