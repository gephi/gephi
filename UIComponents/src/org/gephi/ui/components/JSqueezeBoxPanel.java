/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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

        public void addLayoutComponent(final String name, final Component comp) {
        }

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

        public void removeLayoutComponent(final Component comp) {
        }
    }
}
