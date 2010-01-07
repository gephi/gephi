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
package org.gephi.desktop.filters;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterPanelPanel extends JPanel {

    private Filter selectedFilter;

    public FilterPanelPanel(final FilterUIModel model) {
        super(new BorderLayout());
        model.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (model.getSelectedFilter() != selectedFilter) {
                    selectedFilter = model.getSelectedFilter();
                    setFilter(selectedFilter);
                }
            }
        });
    }

    private void setFilter(Filter filter) {
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        FilterBuilder builder = filterController.getModel().getLibrary().getBuilder(filter);

        //UI update
        removeAll();
        setBorder(null);
        if (filter != null) {
            try {
                JPanel panel = builder.getPanel(filter);
                if (panel != null) {
                    add(panel, BorderLayout.CENTER);
                    setBorder(javax.swing.BorderFactory.createTitledBorder(filter.getName() + " Settings"));
                }
            } catch (Exception e) {
            }
        }

        revalidate();
        repaint();
    }
}
