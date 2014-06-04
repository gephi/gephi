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
package org.gephi.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.gephi.ui.utils.UIUtils;

/**
 *
 * @author Mathieu Bastian
 */
public class JSqueezeBoxPanel extends JPanel {

    private static final Color CP_BACKGROUND_COLOR = UIUtils.getProfilerResultsBackground();
    private final GridBagConstraints gbc = new GridBagConstraints();
    private final JScrollPane scrollPane;
    private final JPanel scrollPanel = new JPanel();
    private final Map<JPanel, SnippetPanel> panelMap = new HashMap<JPanel, SnippetPanel>();

    public JSqueezeBoxPanel() {
        setName("JSqueezeBoxPanel"); // NOI18N
        setLayout(new BorderLayout());

        final SnippetPanel.Padding padding = new SnippetPanel.Padding();

        //final JPanel scrollPanel = new CPMainPanel();     
        scrollPanel.setLayout(new VerticalLayout());

        //GridBagConstraints
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        scrollPanel.add(padding, gbc);

        scrollPane = new JScrollPane(scrollPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CP_BACKGROUND_COLOR);
        scrollPane.getVerticalScrollBar().setUnitIncrement(50);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(50);
        add(scrollPane, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                scrollPane.getVerticalScrollBar().setBlockIncrement((int) (scrollPane.getVerticalScrollBar().getModel().getExtent() * 0.95f));
                scrollPane.getHorizontalScrollBar().setBlockIncrement((int) (scrollPane.getHorizontalScrollBar().getModel().getExtent() * 0.95f));
            }
        });

        setFocusable(true);
        setRequestFocusEnabled(true);
    }

    private void configureSnippetPanel(JPanel panel) {
        panel.setOpaque(true);
        panel.setBackground(CP_BACKGROUND_COLOR);
    }

    public void addPanel(JPanel panel, String name) {
        if (panelMap.containsKey(panel)) {
            return;
        }
        panel.setName(name);
        configureSnippetPanel(panel);
        SnippetPanel snippetPanel = new SnippetPanel(panel.getName(), panel);
        panelMap.put(panel, snippetPanel);
        scrollPanel.add(snippetPanel, gbc);
        scrollPanel.revalidate();
    }

    public void removePanel(JPanel panel) {
        if (!panelMap.containsKey(panel)) {
            return;
        }
        SnippetPanel snippetPanel = panelMap.remove(panel);
        scrollPanel.remove(snippetPanel);
        scrollPanel.revalidate();
    }

    public void cleanPanels() {
        for (SnippetPanel snippetPanel : panelMap.values()) {
            scrollPanel.remove(snippetPanel);
        }
        panelMap.clear();
        scrollPanel.revalidate();
    }

    public static final class VerticalLayout implements LayoutManager {
        //~ Methods --------------------------------------------------------------------------------------------------------------

        @Override
        public void addLayoutComponent(final String name, final Component comp) {
        }

        @Override
        public void layoutContainer(final Container parent) {
            final Insets insets = parent.getInsets();
            final int posX = insets.left;
            int posY = insets.top;
            final int width = parent.getWidth() - insets.left - insets.right;

            final Component[] comps = parent.getComponents();

            for (int i = 0; i < comps.length; i++) {
                final Component comp = comps[i];

                if (comp.isVisible()) {
                    int height = comp.getPreferredSize().height;

                    if (i == (comps.length - 1)) // last component
                    {
                        if ((posY + height) < (parent.getHeight() - insets.bottom)) {
                            height = parent.getHeight() - insets.bottom - posY;
                        }
                    }

                    comp.setBounds(posX, posY, width, height);
                    posY += height;
                }
            }
        }

        @Override
        public Dimension minimumLayoutSize(final Container parent) {
            final Dimension d = new Dimension(parent.getInsets().left + parent.getInsets().right,
                    parent.getInsets().top + parent.getInsets().bottom);
            int maxWidth = 0;
            int height = 0;
            final Component[] comps = parent.getComponents();

            for (int i = 0; i < comps.length; i++) {
                final Component comp = comps[i];

                if (comp.isVisible()) {
                    final Dimension size = comp.getMinimumSize();
                    maxWidth = Math.max(maxWidth, size.width);
                    height += size.height;
                }
            }

            d.width += maxWidth;
            d.height += height;

            return d;
        }

        @Override
        public Dimension preferredLayoutSize(final Container parent) {
            final Dimension d = new Dimension(parent.getInsets().left + parent.getInsets().right,
                    parent.getInsets().top + parent.getInsets().bottom);
            int maxWidth = 0;
            int height = 0;
            final Component[] comps = parent.getComponents();

            for (int i = 0; i < comps.length; i++) {
                final Component comp = comps[i];

                if (comp.isVisible()) {
                    final Dimension size = comp.getPreferredSize();
                    maxWidth = Math.max(maxWidth, size.width);
                    height += size.height;
                }
            }

            d.width += maxWidth;
            d.height += height;

            return d;
        }

        @Override
        public void removeLayoutComponent(final Component comp) {
        }
    }
}
