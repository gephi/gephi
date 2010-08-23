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
package org.gephi.desktop.filters.query;

import javax.swing.Action;
import org.gephi.filters.api.Query;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ParameterNode extends AbstractNode {

    public ParameterNode(String key, Object value) {
        super(Children.LEAF);
        String valStr = value == null ? "null" : value.toString();
        setName(key + ": " + valStr);
        setIconBaseWithExtension("org/gephi/desktop/filters/query/resources/parameter.png");
    }

    public ParameterNode(Query function) {
        super(new ParameterChildren(function));
        setName(NbBundle.getMessage(ParameterNode.class, "ParametersNode.name"));
        setIconBaseWithExtension("org/gephi/desktop/filters/query/resources/parameters.png");
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }
}
