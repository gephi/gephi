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
package org.gephi.sight.explorer;

import java.util.Collection;
import org.gephi.data.network.api.DhnsController;
import org.gephi.data.network.api.SightManager;
import org.gephi.graph.api.Sight;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author Mathieu Bastian
 */
public class SightsChildren extends Children.Keys<Sight> implements LookupListener {

    private Lookup.Result<Sight> result;

    public SightsChildren(Sight sight) {
        DhnsController controller = Lookup.getDefault().lookup(DhnsController.class);
        SightManager sightManager = controller.getSightManager();

        if(sight==null)
        {
            //Init lookup
            result = sightManager.getModelLookup().lookupResult(Sight.class);
            result.addLookupListener(this);
            resultChanged(new LookupEvent(result));
        }
        else
        {
            setKeys(sight.getChildren());
        }
    }

    @Override
    public void resultChanged(LookupEvent lookupEvent) {
         Lookup.Result<Sight> r = (Lookup.Result<Sight>) lookupEvent.getSource();
         Collection<? extends Sight> c = r.allInstances();
         setKeys(c);
    }

    @Override
    protected Node[] createNodes(Sight sight) {
        return new Node[] {new SightsNode(sight)};
    }
}
