/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.visualization.apiimpl.contextmenuitems;

import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Eduardo
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class MoveToWorkspace extends CopyOrMoveToWorkspace{

    @Override
    protected boolean isCopy() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(CopyOrMoveToWorkspace.class, "GraphContextMenu_MoveToWorkspace");
    }

    public int getPosition() {
        return 100;
    }
}
