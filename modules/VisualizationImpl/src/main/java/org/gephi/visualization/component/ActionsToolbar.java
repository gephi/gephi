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
package org.gephi.visualization.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.ui.components.JColorButton;
import org.gephi.ui.utils.UIUtils;
import org.gephi.visualization.VizController;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ActionsToolbar extends JToolBar {

    //Settings
    private Color color = new Color(0.6f, 0.6f, 0.6f);
    private float size = 10.0f;

    public ActionsToolbar() {
        initDesign();
        initContent();
    }

    private void initContent() {

        //Center on graph
        final JButton centerOnGraphButton = new JButton();
        centerOnGraphButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "ActionsToolbar.centerOnGraph"));
        centerOnGraphButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/centerOnGraph.png")));
        centerOnGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VizController.getInstance().getGraphIO().centerOnGraph();
            }
        });
        add(centerOnGraphButton);

        //Center on zero
        /*final JButton centerOnZeroButton = new JButton();
         centerOnZeroButton.setToolTipText(NbBundle.getMessage(VizBarController.class, "ActionsToolbar.centerOnZero"));
         centerOnZeroButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/centerOnZero.png")));
         centerOnZeroButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e) {
         VizController.getInstance().getGraphIO().centerOnZero();
         }
         });
         add(centerOnZeroButton);*/
        //Reset colors
        final JColorButton resetColorButton = new JColorButton(color, true, false);
        resetColorButton.setToolTipText(NbBundle.getMessage(ActionsToolbar.class, "ActionsToolbar.resetColors"));
        resetColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                color = resetColorButton.getColor();
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                GraphModel gm = gc.getGraphModel();
                Graph graph = gm.getGraphVisible();
                for (Node n : graph.getNodes()) {
                    n.setR(color.getRed() / 255f);
                    n.setG(color.getGreen() / 255f);
                    n.setB(color.getBlue() / 255f);
                    n.setAlpha(1f);
                }
                for (Edge e : graph.getEdges()) {
                    e.setR(color.getRed() / 255f);
                    e.setG(color.getGreen() / 255f);
                    e.setB(color.getBlue() / 255f);
                    e.setAlpha(0f);
                }
            }
        });
        add(resetColorButton);

        //Reset sizes
        final JButton resetSizeButton = new JButton();
        resetSizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/resetSize.png")));
        resetSizeButton.setToolTipText(NbBundle.getMessage(ActionsToolbar.class, "ActionsToolbar.resetSizes"));
        resetSizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                GraphModel gm = gc.getGraphModel();
                Graph graph = gm.getGraphVisible();
                for (Node n : graph.getNodes()) {
                    n.setSize(size);
                }
            }
        });
        resetSizeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    Object res = JOptionPane.showInputDialog(resetSizeButton, NbBundle.getMessage(ActionsToolbar.class, "ActionsToolbar.resetSizes.dialog"), "" + size);
                    if (res != null) {
                        try {
                            size = Float.parseFloat((String) res);
                        } catch (Exception ex) {
                        }
                    }
                }
            }
        });
        add(resetSizeButton);

        //Reset label colors
        final JButton resetLabelColorButton = new JButton();
        resetLabelColorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/resetLabelColor.png")));
        resetLabelColorButton.setToolTipText(NbBundle.getMessage(ActionsToolbar.class, "ActionsToolbar.resetLabelColors"));
        resetLabelColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                GraphModel gm = gc.getGraphModel();
                Graph graph = gm.getGraphVisible();
                for (Node n : graph.getNodes().toArray()) {
                    n.getTextProperties().setColor(null);
                }
                for (Edge e : graph.getEdges().toArray()) {
                    e.getTextProperties().setColor(null);
                }
            }
        });
        add(resetLabelColorButton);

        //Reset label colors
        final JButton resetLabelVisibleButton = new JButton();
        resetLabelVisibleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/resetLabelVisible.png")));
        resetLabelVisibleButton.setToolTipText(NbBundle.getMessage(ActionsToolbar.class, "ActionsToolbar.resetLabelVisible"));
        resetLabelVisibleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                GraphModel gm = gc.getGraphModel();
                Graph graph = gm.getGraphVisible();
                for (Node n : graph.getNodes()) {
                    n.getTextProperties().setVisible(true);
                }
                for (Edge e : graph.getEdges()) {
                    e.getTextProperties().setVisible(true);
                }
            }
        });
        add(resetLabelVisibleButton);

        //Reset label size
        JButton resetLabelSizeButton = new JButton();
        resetLabelSizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/gephi/visualization/component/resetLabelSize.png")));
        resetLabelSizeButton.setToolTipText(NbBundle.getMessage(ActionsToolbar.class, "ActionsToolbar.resetLabelSizes"));
        resetLabelSizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GraphController gc = Lookup.getDefault().lookup(GraphController.class);
                GraphModel gm = gc.getGraphModel();
                Graph graph = gm.getGraphVisible();
                for (Node n : graph.getNodes()) {
                    n.getTextProperties().setSize(1f);
                }
            }
        });
        add(resetLabelSizeButton);
    }

    private void initDesign() {
        setFloatable(false);
        setOrientation(JToolBar.VERTICAL);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE); //NOI18N
        setBorder(BorderFactory.createEmptyBorder(0, 2, 15, 2));
        setOpaque(false);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                for (Component c : getComponents()) {
                    c.setEnabled(enabled);
                }
            }
        });
    }

    @Override
    public Component add(Component comp) {
        if (comp instanceof JButton) {
            UIUtils.fixButtonUI((JButton) comp);
        }
        return super.add(comp);
    }
}
