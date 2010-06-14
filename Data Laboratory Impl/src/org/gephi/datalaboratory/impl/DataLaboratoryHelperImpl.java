/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalaboratory.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.gephi.datalaboratory.api.DataLaboratoryHelper;
import org.gephi.datalaboratory.spi.Manipulator;
import org.gephi.datalaboratory.spi.edges.EdgesManipulator;
import org.gephi.datalaboratory.spi.edges.EdgesManipulatorBuilder;
import org.gephi.datalaboratory.spi.nodes.NodesManipulator;
import org.gephi.datalaboratory.spi.nodes.NodesManipulatorBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Implementation of the DataLaboratoryHelper interface
 * declared in the Data Laboratory API.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 * @see DataLaboratoryHelper
 */
@ServiceProvider(service=DataLaboratoryHelper.class)
public class DataLaboratoryHelperImpl implements DataLaboratoryHelper{

    public NodesManipulator[] getNodesManipulators() {
        ArrayList<NodesManipulator> nodesManipulators=new ArrayList<NodesManipulator>();
        for(NodesManipulatorBuilder nm:Lookup.getDefault().lookupAll(NodesManipulatorBuilder.class)){
            nodesManipulators.add(nm.getNodesManipulator());
        }
        sortGraphElementsManipulators(nodesManipulators);
        return nodesManipulators.toArray(new NodesManipulator[0]);
    }

    public EdgesManipulator[] getEdgesManipulators() {
        ArrayList<EdgesManipulator> edgesManipulators=new ArrayList<EdgesManipulator>();
        for(EdgesManipulatorBuilder em:Lookup.getDefault().lookupAll(EdgesManipulatorBuilder.class)){
            edgesManipulators.add(em.getEdgesManipulator());
        }
        sortGraphElementsManipulators(edgesManipulators);
        return edgesManipulators.toArray(new EdgesManipulator[0]);
    }

    private void sortGraphElementsManipulators(ArrayList<? extends Manipulator> m){
        Collections.sort(m, new Comparator<Manipulator>(){

            public int compare(Manipulator o1, Manipulator o2) {
                return o1.getPosition()-o2.getPosition();
            }
        });
    }
}
