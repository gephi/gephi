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
package org.gephi.desktop.statistics;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.gephi.desktop.statistics.api.StatisticsControllerUI;
import org.gephi.desktop.statistics.api.StatisticsModelUI;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.ui.components.JSqueezeBoxPanel;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class AvailableStatisticsChooser extends javax.swing.JPanel {

    private final JSqueezeBoxPanel squeezeBoxPanel = new JSqueezeBoxPanel();
    private final Map<JCheckBox, StatisticsUI> uiMap = new HashMap<>();

    public AvailableStatisticsChooser() {
        initComponents();
        metricsPanel.add(squeezeBoxPanel, BorderLayout.CENTER);
    }

    public void setup(StatisticsModelUI model, StatisticsCategory[] categories) {

        //Sort categories by position
        Arrays.sort(categories, new Comparator() {

            @Override
            public int compare(Object o1, Object o2) {
                Integer p1 = ((StatisticsCategory) o1).getPosition();
                Integer p2 = ((StatisticsCategory) o2).getPosition();
                return p1.compareTo(p2);
            }
        });

        //Get UI
        StatisticsUI[] statisticsUIs = Lookup.getDefault().lookupAll(StatisticsUI.class).toArray(new StatisticsUI[0]);

        for (StatisticsCategory category : categories) {
            MigLayout migLayout = new MigLayout("insets 0 0 0 0");
            migLayout.setColumnConstraints("[grow,fill]");
            migLayout.setRowConstraints("[min!]");
            JPanel innerPanel = new JPanel(migLayout);

            //Find uis in this category
            List<StatisticsUI> uis = new ArrayList<>();
            for (StatisticsUI sui : statisticsUIs) {
                if (sui.getCategory().equals(category.getName())) {
                    uis.add(sui);
                }
            }

            //Sort it by position
            Collections.sort(uis, new Comparator() {

                @Override
                public int compare(Object o1, Object o2) {
                    Integer p1 = ((StatisticsUI) o1).getPosition();
                    Integer p2 = ((StatisticsUI) o2).getPosition();
                    return p1.compareTo(p2);
                }
            });

            for (StatisticsUI sui : uis) {
                JCheckBox checkBox = new JCheckBox(sui.getDisplayName());
                checkBox.setOpaque(false);
                checkBox.setSelected(model.isStatisticsUIVisible(sui));
                uiMap.put(checkBox, sui);
                innerPanel.add(checkBox, "wrap");
            }

            if (uis.size() > 0) {
                squeezeBoxPanel.addPanel(innerPanel, category.getName());
            }
        }
    }

    public void unsetup() {
        //Only called when OK
        StatisticsControllerUI controller = Lookup.getDefault().lookup(StatisticsControllerUI.class);

        for (Map.Entry<JCheckBox, StatisticsUI> entry : uiMap.entrySet()) {
            controller.setStatisticsUIVisible(entry.getValue(), entry.getKey().isSelected());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        metricsPanel = new javax.swing.JPanel();

        metricsPanel.setLayout(new java.awt.BorderLayout());

        metricsPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(metricsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 396, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(metricsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(metricsPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel metricsPanel;
    // End of variables declaration//GEN-END:variables
}
