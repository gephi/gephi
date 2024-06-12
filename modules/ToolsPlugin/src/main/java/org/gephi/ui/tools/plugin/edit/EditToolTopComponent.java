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

package org.gephi.ui.tools.plugin.edit;

import javax.swing.SwingUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@ConvertAsProperties(dtd = "-//org.gephi.ui.tools.plugin.edit//EditTool//EN",
    autostore = false)
@TopComponent.Description(preferredID = "EditToolTopComponent",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "rankingmode", openAtStartup = false, roles = {"overview", "datalab"})
@TopComponent.OpenActionRegistration(displayName = "#CTL_EditToolTopComponent",
    preferredID = "EditToolTopComponent")
public final class EditToolTopComponent extends TopComponent {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel propertySheet;
    // End of variables declaration//GEN-END:variables

    public EditToolTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(EditToolTopComponent.class, "CTL_EditToolTopComponent"));

        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        ((PropertySheet) propertySheet).setDescriptionAreaVisible(false);

        Lookup.getDefault().lookup(ProjectController.class).addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
                SwingUtilities.invokeLater(() -> {
                    propertySheet.setEnabled(true);
                });
            }

            @Override
            public void unselect(Workspace workspace) {
                disableEdit();
            }

            @Override
            public void close(Workspace workspace) {
            }

            @Override
            public void disable() {
                SwingUtilities.invokeLater(() -> {
                    propertySheet.setEnabled(false);
                    EditToolTopComponent.this.close();
                });
            }
        });
    }

    public void editNode(Node node) {
        ((PropertySheet) propertySheet).setNodes(new org.openide.nodes.Node[] {new EditNodes(node)});
    }

    public void editNodes(Node[] nodes) {
        ((PropertySheet) propertySheet).setNodes(new org.openide.nodes.Node[] {new EditNodes(nodes)});
    }

    public void editEdge(Edge edge) {
        ((PropertySheet) propertySheet).setNodes(new org.openide.nodes.Node[] {new EditEdges(edge)});
    }

    public void editEdges(Edge[] edges) {
        ((PropertySheet) propertySheet).setNodes(new org.openide.nodes.Node[] {new EditEdges(edges)});
    }

    public void disableEdit() {
        ((PropertySheet) propertySheet).setNodes(new org.openide.nodes.Node[] {});
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        propertySheet = new PropertySheet();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        add(propertySheet, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

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
