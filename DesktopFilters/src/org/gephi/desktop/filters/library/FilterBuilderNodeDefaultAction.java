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
package org.gephi.desktop.filters.library;

import java.awt.event.ActionEvent;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.spi.FilterBuilder;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Mathieu Bastian
 */
public class FilterBuilderNodeDefaultAction extends SystemAction {

    public static FilterBuilderNodeDefaultAction instance = new FilterBuilderNodeDefaultAction();

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HelpCtx getHelpCtx() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        FilterBuilderNode node = (FilterBuilderNode) ev.getSource();
        FilterBuilder builder = node.getBuilder();
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        Query function = filterController.createQuery(builder.getFilter());
        filterController.add(function);
    }
}
