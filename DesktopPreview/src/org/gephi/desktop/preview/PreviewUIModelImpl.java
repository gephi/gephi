/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.preview;

import org.gephi.desktop.preview.api.PreviewUIModel;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewPreset;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.presets.DefaultPreset;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class PreviewUIModelImpl implements PreviewUIModel {

    //Data
    private float visibilityRatio = 1f;
    private PreviewPreset currentPreset;
    private boolean refreshing;
    private boolean workspaceBarVisible;

    public PreviewUIModelImpl() {
        currentPreset = new DefaultPreset();
    }

    public PreviewPreset getCurrentPreset() {
        return currentPreset;
    }

    public float getVisibilityRatio() {
        return visibilityRatio;
    }

    public void setVisibilityRatio(float visibilityRatio) {
        this.visibilityRatio = visibilityRatio;
    }

    public void setCurrentPreset(PreviewPreset preset) {
        currentPreset = preset;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public boolean isWorkspaceBarVisible() {
        return workspaceBarVisible;
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public void setWorkspaceBarVisible(boolean workspaceBarVisible) {
        this.workspaceBarVisible = workspaceBarVisible;
    }
}
