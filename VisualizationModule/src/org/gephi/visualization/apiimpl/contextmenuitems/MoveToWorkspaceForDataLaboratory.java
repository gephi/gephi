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

import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.datalab.spi.nodes.NodesManipulatorBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=NodesManipulatorBuilder.class)
public class MoveToWorkspaceForDataLaboratory implements NodesManipulatorBuilder{

    public NodesManipulator getNodesManipulator() {
        return new MoveToWorkspaceForDataLaboratoryManipulator();
    }
}

/**
 * Same action as MoveToWorkspace, with different position for data laboratory.
 * @author Eduardo
 */
class MoveToWorkspaceForDataLaboratoryManipulator extends MoveToWorkspace{

    @Override
    public int getType() {
        return 100;
    }

    @Override
    public int getPosition() {
        return 400;
    }
}
