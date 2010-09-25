/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.desktop.filters;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.ui.utils.UIUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterPanelPanel extends JPanel implements ChangeListener {

    private Query selectedQuery;
    private final String settingsString;
    private FilterUIModel uiModel;

    public FilterPanelPanel() {
        super(new BorderLayout());
        settingsString = NbBundle.getMessage(FilterPanelPanel.class, "FilterPanelPanel.settings");
        if (UIUtils.isAquaLookAndFeel()) {
            setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }

    public void stateChanged(ChangeEvent e) {
        refreshModel();
    }

    private void refreshModel() {
        if (uiModel != null) {
            if (uiModel.getSelectedQuery() != selectedQuery) {
                selectedQuery = uiModel.getSelectedQuery();
                setQuery(selectedQuery);
            }
        } else {
            setQuery(null);
        }
    }

    public void setup(FilterUIModel model) {
        uiModel = model;
        if (model != null) {
            model.addChangeListener(this);
        }
        refreshModel();
    }

    public void unsetup() {
        if (uiModel != null) {
            uiModel.removeChangeListener(this);
            uiModel = null;
            refreshModel();
        }
    }

    private void setQuery(final Query query) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //UI update
                removeAll();
                setBorder(null);
                if (query != null) {
                    FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                    FilterBuilder builder = filterController.getModel().getLibrary().getBuilder(query.getFilter());
                    try {
                        JPanel panel = builder.getPanel(query.getFilter());
                        if (panel != null) {
                            add(panel, BorderLayout.CENTER);
                            panel.setOpaque(false);
                            setBorder(javax.swing.BorderFactory.createTitledBorder(query.getFilter().getName() + " " + settingsString));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                revalidate();
                repaint();
            }
        });

    }
}
