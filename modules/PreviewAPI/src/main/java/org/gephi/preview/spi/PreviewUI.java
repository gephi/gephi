/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
