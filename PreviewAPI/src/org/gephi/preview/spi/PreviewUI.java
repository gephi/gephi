/*
Copyright 2008-2010 Gephi
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
package org.gephi.preview.spi;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;

/**
 * SPI interface to add UI settings components to the Preview.
 * <p>
 * Implementations of this interface provide a <code>JPanel</code> component
 * which will be placed in a separated tab in the Preview Settings. When a workspace is
 * selected this class receives the current {@link PreviewModel} so the panel
 * can access the central {@link PreviewProperties} and can read/write settings
 * values.
 * <p>
 * PreviewUI are singleton services and implementations need to add the
 * following annotation to be recognized by the system:
 * <p>
 * <code>@ServiceProvider(service=PreviewUI.class)</code>
 * 
 * @author Mathieu Bastian
 */
public interface PreviewUI {

    /**
     * Initialization method called when a workspace is selected and a panel is
     * about to be requested. The system first calls this method and then
     * <code>getPanel()</code>.
     * @param previewModel the model associated to the current workspace
     */
    public void setup(PreviewModel previewModel);

    /**
     * Returns the <code>JPanel</code> component to be displayed. 
     * <p>
     * This method
     * is always called <b>after</b> <code>setup()</code> so the implementation
     * can initialize the panel with the model. Note that the panel is destroyed
     * after <code>unsetup()</code> is called. In other words, a new panel is
     * requested at each workspace selection.
     * @return the panel to be displayed
     */
    public JPanel getPanel();

    /**
     * Method called when the UI is unloaded and the panel to be destroyed. This
     * happens when the workspace changes and before a new <code>PreviewModel</code>
     * is passed through <code>setup()</code>.
     */
    public void unsetup();

    /**
     * Returns the icon of the tab or <code>null</code> if none
     * @return the tab's icon or <code>null</code>
     */
    public Icon getIcon();

    /**
     * Returns the title of the tab
     * @return the tab's title
     */
    public String getPanelTitle();
}
