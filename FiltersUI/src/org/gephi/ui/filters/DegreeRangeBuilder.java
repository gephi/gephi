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
package org.gephi.ui.filters;

import org.gephi.filters.*;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.Filter;
import org.gephi.filters.api.FilterBuilder;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class DegreeRangeBuilder implements FilterBuilder {

    public String getName() {
        return NbBundle.getMessage(DegreeRangeBuilder.class, "Filters.DegreeRangeBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public DegreeRangeFilter getFilter() {
        return new DegreeRangeFilter();
    }

    public Class getFilterClass() {
        return DegreeRangeFilter.class;
    }

    public JPanel getUI(Filter filter) {
        if (!(filter instanceof DegreeRangeFilter)) {
            throw new IllegalArgumentException("The filter must be an instance of DegreeRangeFilter");
        }
        DegreeRangePanel panel = new DegreeRangePanel();
        panel.setup((DegreeRangeFilter) filter);
        return panel;
    }
}
