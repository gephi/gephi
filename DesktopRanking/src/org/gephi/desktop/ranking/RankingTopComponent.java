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
package org.gephi.desktop.ranking;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

final class RankingTopComponent extends TopComponent implements Lookup.Provider, PropertyChangeListener {

    private static RankingTopComponent instance;
    static final String ICON_PATH = "org/gephi/desktop/ranking/resources/small.png";
    private static final String PREFERRED_ID = "RankingTopComponent";
    //UI
    private JToggleButton listButton;
    //Model
    private transient RankingUIController controller;
    private transient RankingUIModel model;
    private transient ChangeListener modelChangeListener;

    private RankingTopComponent() {
        setName(NbBundle.getMessage(RankingTopComponent.class, "CTL_RankingTopComponent"));
//        setToolTipText(NbBundle.getMessage(RankingTopComponent.class, "HINT_RankingTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH));

        modelChangeListener = new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                refreshModel(ce == null ? null : (RankingUIModel) ce.getSource());
            }
        };
        controller = Lookup.getDefault().lookup(RankingUIController.class);
        controller.setModelChangeListener(modelChangeListener);
        model = controller.getModel();

        initComponents();
        initSouth();
        if (UIUtils.isAquaLookAndFeel()) {
            mainPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        refreshModel(model);
    }

    public void refreshModel(RankingUIModel model) {
        if (this.model != null) {
            this.model.removePropertyChangeListener(this);
        }
        this.model = model;
        if (model != null) {
            model.addPropertyChangeListener(this);
        }
        refreshEnable();

        //South visible
        /*if (barChartPanel.isVisible() != model.isBarChartVisible()) {
        barChartPanel.setVisible(model.isBarChartVisible());
        revalidate();
        repaint();
        }*/
        ((ResultListPanel) listResultPanel).unselect();
        if (model != null) {
            ((ResultListPanel) listResultPanel).select(model);
            if (listResultPanel.isVisible() != model.isListVisible()) {
                listResultPanel.setVisible(model.isListVisible());
                revalidate();
                repaint();
            }

            //barChartButton.setSelected(model.isBarChartVisible());
            listButton.setSelected(model.isListVisible());
        } else {
            listResultPanel.setVisible(false);
            listButton.setSelected(false);
        }


        //Chooser
        ((RankingChooser) rankingChooser).refreshModel(model);

        //Toolbar
        ((RankingToolbar) rankingToolbar).refreshModel(model);
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getPropertyName().equals(RankingUIModel.LIST_VISIBLE)) {
            listButton.setSelected((Boolean) pce.getNewValue());
            if (listResultPanel.isVisible() != model.isListVisible()) {
                listResultPanel.setVisible(model.isListVisible());
                revalidate();
                repaint();
            }
        } else if (pce.getPropertyName().equals(RankingUIModel.BARCHART_VISIBLE)) {
            //barChartButton.setSelected((Boolean)pce.getNewValue());
        }
    }

    private void initSouth() {
        listButton = new JToggleButton();
        listButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/list.png"))); // NOI18N
        NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.listButton.text");
        listButton.setEnabled(false);
        listButton.setFocusable(false);
        southToolbar.add(listButton);
        /*barChartButton = new JToggleButton();
        barChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/desktop/ranking/resources/barchart.png"))); // NOI18N
        NbBundle.getMessage(RankingTopComponent.class, "RankingTopComponent.barchartButton.text");
        barChartButton.setEnabled(false);
        barChartButton.setFocusable(false);
        southToolbar.add(barChartButton);*/

        //BarChartPanel & ListPanel
        listResultPanel.setVisible(false);

        listButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                model.setListVisible(listButton.isSelected());
            }
        });

        /*barChartButton.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
        model.setBarChartVisible(barChartButton.isSelected());
        }
        });*/
    }

    private void refreshEnable() {
        boolean modelEnabled = isModelEnabled();

        //barChartButton.setEnabled(modelEnabled);
        listButton.setEnabled(modelEnabled);
        rankingChooser.setEnabled(modelEnabled);
        rankingToolbar.setEnabled(modelEnabled);
        listResultPanel.setEnabled(modelEnabled);
    }

    private boolean isModelEnabled() {
        return model != null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        rankingToolbar = new RankingToolbar(controller);
        rankingChooser = new RankingChooser(controller);
        listResultContainerPanel = new javax.swing.JPanel();
        listResultPanel = new ResultListPanel();
        southToolbar = new javax.swing.JToolBar();

        setOpaque(true);
        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        rankingToolbar.setFloatable(false);
        rankingToolbar.setRollover(true);
        rankingToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(rankingToolbar, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(rankingChooser, gridBagConstraints);

        listResultContainerPanel.setOpaque(false);
        listResultContainerPanel.setLayout(new java.awt.GridLayout(1, 0));

        listResultPanel.setBorder(null);
        listResultContainerPanel.add(listResultPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 1, 5);
        mainPanel.add(listResultContainerPanel, gridBagConstraints);

        southToolbar.setFloatable(false);
        southToolbar.setRollover(true);
        southToolbar.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        mainPanel.add(southToolbar, gridBagConstraints);

        add(mainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel listResultContainerPanel;
    private javax.swing.JScrollPane listResultPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel rankingChooser;
    private javax.swing.JToolBar rankingToolbar;
    private javax.swing.JToolBar southToolbar;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized RankingTopComponent getDefault() {
        if (instance == null) {
            instance = new RankingTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the RankingTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized RankingTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(RankingTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof RankingTopComponent) {
            return (RankingTopComponent) win;
        }
        Logger.getLogger(RankingTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    /** replaces this in object stream */
    @Override
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    final static class ResolvableHelper implements Serializable {

        private static final long serialVersionUID = 1L;

        public Object readResolve() {
            return RankingTopComponent.getDefault();
        }
    }
}
