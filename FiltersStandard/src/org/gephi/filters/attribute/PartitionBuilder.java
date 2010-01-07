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
package org.gephi.filters.attribute;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.filters.spi.FilterProperty;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionBuilder implements FilterBuilder {

    public Category getCategory() {
        return FilterLibrary.ATTRIBUTES;
    }

    public String getName() {
        return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name");
    }

    public Icon getIcon() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public PartitionFilter getFilter() {
        return new PartitionFilter();
    }

    public JPanel getPanel(Filter filter) {
        PartitionUI ui = Lookup.getDefault().lookup(PartitionUI.class);
        if (ui != null) {
            return ui.getPanel((PartitionFilter) filter);
        }
        return null;
    }

    public static class PartitionFilter implements Filter {

        private FilterProperty[] filterProperties;
        private String pattern;
        private boolean useRegex;

        public String getName() {
            return NbBundle.getMessage(PartitionBuilder.class, "PartitionBuilder.name");
        }

        public FilterProperty[] getProperties() {
            if (filterProperties == null) {
                filterProperties = new FilterProperty[0];
            }
            return filterProperties;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isUseRegex() {
            return useRegex;
        }

        public void setUseRegex(boolean useRegex) {
            this.useRegex = useRegex;
        }
    }
}
