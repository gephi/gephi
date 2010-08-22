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

import javax.swing.Action;
import org.gephi.filters.api.Query;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 *
 * @author Mathieu Bastian
 */
public class SavedQueryNode extends AbstractNode {

    private Query query;

    public SavedQueryNode(Query query) {
        super(Children.LEAF);
        this.query = query;
        setDisplayName(getQueryName(query));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[0];
    }

    @Override
    public Action getPreferredAction() {
        return SavedQueryNodeDefaultAction.instance;
    }

    public Query getQuery() {
        return query;
    }

    private String getQueryName(Query query) {
        String res = query.getName();
        if (query.getPropertiesCount() > 0) {
            res += "(";
            for (int i = 0; i < query.getPropertiesCount(); i++) {
                res += "'" + query.getPropertyValue(i).toString() + "'";
                res += (i + 1 < query.getPropertiesCount()) ? "," : "";
            }
        }
        if (query.getChildren() != null) {
            if (query.getPropertiesCount() == 0) {
                res += "(";
            } else {
                res += ",";
            }
            for (Query child : query.getChildren()) {
                res += getQueryName(child);
                res += ",";
            }
            if (res.endsWith(",")) {
                res = res.substring(0, res.length() - 1);
            }
        }
        res += ")";
        return res;
    }
}
