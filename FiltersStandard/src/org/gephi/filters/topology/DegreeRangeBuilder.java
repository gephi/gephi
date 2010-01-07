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
package org.gephi.filters.topology;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.api.Range;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service=FilterBuilder.class)
public class DegreeRangeBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    public String getName() {
        return NbBundle.getMessage(DegreeRangeBuilder.class, "DegreeRangeBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public DegreeRangeFilter getFilter() {
        return new DegreeRangeFilter();
    }

    public JPanel getPanel(Filter filter) {
        RangeUI ui = Lookup.getDefault().lookup(RangeUI.class);
        if (ui != null) {
            return ui.getPanel(filter.getProperties()[0]);
        }
        return null;
    }

    public static class DegreeRangeFilter implements Filter {

        private Range range = new Range(0, 0);

        public String getName() {
            return NbBundle.getMessage(DegreeRangeBuilder.class, "DegreeRangeBuilder.name");
        }

        public FilterProperty[] getProperties() {
            try {
                return new FilterProperty[]{
                            FilterProperty.createProperty(this, Range.class, "range")
                        };
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
            return new FilterProperty[0];
        }

        public Range getRange() {
            return range;
        }

        public void setRange(Range range) {
            this.range = range;
        }
    }
}
