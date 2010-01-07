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

import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.Operator;

/**
 *
 * @author Mathieu Bastian
 */
public class OperatorQueryImpl extends AbstractQueryImpl {

    private Operator operator;

    public OperatorQueryImpl(Operator predicate) {
        this.operator = (Operator) predicate;
    }

    @Override
    public int getChildrenSlotsCount() {
        return operator.getInputCount();
    }

    public String getName() {
        return operator.getName();
    }

    @Override
    public void setName(String name) {
    }

    public int getParametersCount() {
        return 0;
    }

    public String getParameterName(int index) {
        return null;
    }

    public Object getParameterValue(int index) {
        return null;
    }

    public Filter getFilter() {
        return operator;
    }
}
