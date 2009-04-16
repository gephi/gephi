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

import javax.swing.Action;
import org.gephi.graph.api.Sight;
import org.gephi.sight.explorer.actions.AddSight;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class SightsNode extends AbstractNode {

    private Sight sight;
    private boolean rootNode=false;

    public SightsNode(Sight sight)
    {
        super(((sight==null) || (sight!=null && sight.hasChildren()))?(new SightsChildren(sight)):Children.LEAF);
        this.sight = sight;
        if(sight==null)
            rootNode=true;
    }

    @Override
    public String getDisplayName() {
        if(rootNode)
            return "Sights";
        else
        {
            if(sight.getName()!=null)
                return sight.getName();
            else
                return NbBundle.getMessage(SightsNode.class, "SightsNode_default_sight_name")+" "+sight.getNumber();
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        if(rootNode)
            return new Action[] { new AddSight() };
        return new Action[] {};
    }

}
