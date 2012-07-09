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
package org.gephi.desktop.partition;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.api.PartitionModel;
import org.gephi.partition.spi.TransformerBuilder;
import org.gephi.partition.spi.TransformerUI;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionToolbar extends JToolBar implements PropertyChangeListener {

    //Architecture
    private PartitionModel model;

    public PartitionToolbar() {
        initComponents();
        initTransformersUI();
        refreshModel();
    }

    public void setup(PartitionModel model) {
        if (model != null) {
            this.model = model;
            model.addPropertyChangeListener(this);
            refreshModel();
        }
    }

    public void unsetup() {
        if (model != null) {
            model.removePropertyChangeListener(this);
            model = null;
        }
        refreshModel();
    }

    private void refreshModel() {
        if (model == null) {
            setEnabled(false);
            return;
        } else {
            setEnabled(true);
        }
        boolean nodeSelected = model.getSelectedPartitioning() == PartitionModel.NODE_PARTITIONING;
        boolean edgeSelected = !nodeSelected;
        elementGroup.setSelected(nodeSelected ? nodeButton.getModel() : edgeButton.getModel(), true);

        nodeTransformerGroup.clearSelection();
        edgeTransformerGroup.clearSelection();

        for (Enumeration<AbstractButton> btns = nodeTransformerGroup.getElements(); btns.hasMoreElements();) {
            AbstractButton btn = btns.nextElement();
            btn.setVisible(nodeSelected);
            if (model.getNodeTransformerBuilder() != null && btn.getName().equals(model.getNodeTransformerBuilder().getClass().getName())) {
                nodeTransformerGroup.setSelected(btn.getModel(), true);
            }
        }
        for (Enumeration<AbstractButton> btns = edgeTransformerGroup.getElements(); btns.hasMoreElements();) {
            AbstractButton btn = btns.nextElement();
            btn.setVisible(edgeSelected);
            if (model.getEdgeTransformerBuilder() != null && btn.getName().equals(model.getEdgeTransformerBuilder().getClass().getName())) {
                edgeTransformerGroup.setSelected(btn.getModel(), true);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PartitionModel.SELECTED_PARTIONING)) {
            refreshModel();
        } else if (evt.getPropertyName().equals(PartitionModel.NODE_TRANSFORMER)) {
            refreshModel();
        } else if (evt.getPropertyName().equals(PartitionModel.EDGE_TRANSFORMER)) {
            refreshModel();
        }
    }

    private void initTransformersUI() {
        nodeTransformerGroup = new ButtonGroup();
        edgeTransformerGroup = new ButtonGroup();

        TransformerBuilder[] builders = Lookup.getDefault().lookupAll(TransformerBuilder.class).toArray(new TransformerBuilder[0]);

        for (final TransformerBuilder t : builders) {
            TransformerUI transformerUI = t.getUI();
            JToggleButton btn = new JToggleButton(transformerUI.getIcon());
            btn.setToolTipText(transformerUI.getName());
            btn.setVisible(false);
            btn.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
                    pc.setSelectedTransformerBuilder(t);
                }
            });
            btn.setName(t.getClass().getName());
            btn.setFocusPainted(false);
            if (t instanceof TransformerBuilder.Node) {
                nodeTransformerGroup.add(btn);
            } else {
                edgeTransformerGroup.add(btn);
            }
            add(btn);
        }

        //Init first
        /*if (!nodeTrans.isEmpty()) {
        model.setNodeTransformer(nodeTrans.get(0).getTransformerClass());
        }
        if (!edgeTrans.isEmpty()) {
        model.setEdgeTransformer(edgeTrans.get(0).getTransformerClass());
        }*/
    }

    private void initComponents() {
        elementGroup = new javax.swing.ButtonGroup();
        nodeButton = new javax.swing.JToggleButton();
        edgeButton = new javax.swing.JToggleButton();
        nodeButton.setFocusPainted(false);
        edgeButton.setFocusPainted(false);
        box = new javax.swing.JLabel();

        setFloatable(false);
        setRollover(true);
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        setBorder(b);
        if (UIUtils.isAquaLookAndFeel()) {
            setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        elementGroup.add(nodeButton);
        nodeButton.setText(NbBundle.getMessage(PartitionToolbar.class, "PartitionToolbar.nodes.label"));
        nodeButton.setEnabled(false);
        add(nodeButton);

        elementGroup.add(edgeButton);
        edgeButton.setText(NbBundle.getMessage(PartitionToolbar.class, "PartitionToolbar.edges.label"));
        edgeButton.setEnabled(false);
        add(edgeButton);
        addSeparator();

        box.setMaximumSize(new java.awt.Dimension(32767, 32767));
        add(box);

        nodeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
                pc.setSelectedPartitioning(PartitionModel.NODE_PARTITIONING);
            }
        });
        edgeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                PartitionController pc = Lookup.getDefault().lookup(PartitionController.class);
                pc.setSelectedPartitioning(PartitionModel.EDGE_PARTITIONING);
            }
        });
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });
    }
    private javax.swing.JLabel box;
    private javax.swing.JToggleButton edgeButton;
    private javax.swing.ButtonGroup elementGroup;
    private javax.swing.ButtonGroup nodeTransformerGroup;
    private javax.swing.ButtonGroup edgeTransformerGroup;
    private javax.swing.JToggleButton nodeButton;
}
