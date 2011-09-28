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
package org.gephi.desktop.preview.api;

import java.beans.PropertyChangeListener;
import org.gephi.preview.api.PreviewPreset;

/**
 *
 * @author Mathieu Bastian
 */
public interface PreviewUIController {

    //Property
    public static final String SELECT = "select";
    public static final String UNSELECT = "unselect";
    public static final String REFRESHED = "refreshed";
    public static final String REFRESHING = "refreshing";
    public static final String GRAPH_CHANGED = "graph_changed";

    public void refreshPreview();

    public void setCurrentPreset(PreviewPreset preset);

    public void setVisibilityRatio(float visibilityRatio);

    public PreviewPreset[] getDefaultPresets();

    public PreviewPreset[] getUserPresets();

    public void addPreset(PreviewPreset preset);

    public void savePreset(String name);

    public PreviewUIModel getModel();

    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void removePropertyChangeListener(PropertyChangeListener listener);
}
