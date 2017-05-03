/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.desktop.filters;

import java.awt.BorderLayout;
import java.util.TimerTask;
import org.gephi.filters.api.FilterModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.gephi.desktop.filters//Filters//EN",
        autostore = false)
@TopComponent.Description(preferredID = "FiltersTopComponent",
        iconBase = "org/gephi/desktop/filters/resources/small.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "filtersmode", openAtStartup = true, roles = {"overview"})
@ActionID(category = "Window", id = "org.gephi.desktop.filters.FiltersTopComponent")
@ActionReference(path = "Menu/Window", position = 400)
@TopComponent.OpenActionRegistration(displayName = "#CTL_FiltersTopComponent",
        preferredID = "FiltersTopComponent")
public final class FiltersTopComponent extends TopComponent {

    private static FiltersTopComponent instance;
    static final String ICON_PATH = "org/gephi/desktop/filters/resources/small.png";
    private static final String PREFERRED_ID = "FiltersTopComponent";
    //Panel
    private final FiltersPanel panel;
    //Models
    private FilterModel filterModel;
    private WorkspaceColumnsObservers observers;
    private FilterUIModel uiModel;
    
    private static final long AUTO_REFRESH_RATE_MILLISECONDS = 3000;
    private java.util.Timer observersTimer;

    public FiltersTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(FiltersTopComponent.class, "CTL_FiltersTopComponent"));
//        setToolTipText(NbBundle.getMessage(FiltersTopComponent.class, "HINT_FiltersTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);

        panel = new FiltersPanel();
        add(panel, BorderLayout.CENTER);

        //Model management
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
                workspace.add(new FilterUIModel(workspace));
            }

            @Override
            public void select(Workspace workspace) {
                activateWorkspace(workspace);
                refreshModel();
            }

            @Override
            public void unselect(Workspace workspace) {
                observers.destroy();
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                filterModel = null;
                uiModel = null;
                observers = null;
                refreshModel();
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            Workspace workspace = pc.getCurrentWorkspace();
            activateWorkspace(workspace);
        }
        refreshModel();
        
        initEvents();
    }

    private void activateWorkspace(Workspace workspace) {
        filterModel = workspace.getLookup().lookup(FilterModel.class);
        uiModel = workspace.getLookup().lookup(FilterUIModel.class);
        if (uiModel == null) {
            uiModel = new FilterUIModel(workspace);
            workspace.add(uiModel);
        }

        observers = workspace.getLookup().lookup(WorkspaceColumnsObservers.class);
        if (observers == null) {
            workspace.add(observers = new WorkspaceColumnsObservers(workspace));
        }
        observers.initialize();
    }

    private void refreshModel() {
        panel.refreshModel(filterModel, uiModel);
    }

    public FilterUIModel getUiModel() {
        return uiModel;
    }
    
    private void initEvents() {
        observersTimer = new java.util.Timer("DataLaboratoryGraphObservers");
        observersTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                if(observers != null) {
                    if(observers.hasChanges()) {
                        refreshModel();
                    }
                }
            }
        }
        , 0, AUTO_REFRESH_RATE_MILLISECONDS);//Check graph and tables for changes every 100 ms
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
