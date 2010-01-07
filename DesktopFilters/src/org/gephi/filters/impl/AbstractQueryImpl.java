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
package org.gephi.filters.impl;

import java.util.ArrayList;
import java.util.List;
import org.gephi.filters.api.Query;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class AbstractQueryImpl implements Query {

    protected List<Query> children;
    protected Query parent;

    public AbstractQueryImpl() {
        this.children = new ArrayList<Query>();
    }

    public abstract int getChildrenSlotsCount();

    public abstract int getParametersCount();

    public abstract String getParameterName(int index);

    public abstract Object getParameterValue(int index);

    public abstract String getName();

    public abstract void setName(String name);

    public Query[] getChildren() {
        return children.toArray(new Query[0]);
    }

    public int getChildrenCount() {
        return children.size();
    }

    public void addSubQuery(Query subQuery) {
        children.add(subQuery);
        ((AbstractQueryImpl) subQuery).setParent(this);
    }

    public void removeSubQuery(Query subQuery) {
        children.remove(subQuery);
    }

    public Query getParent() {
        return parent;
    }

    public void setParent(Query parent) {
        this.parent = parent;
    }
}
