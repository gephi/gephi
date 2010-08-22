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
package org.gephi.filters;

import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.Operator;

/**
 *
 * @author Mathieu Bastian
 */
public class OperatorQueryImpl extends AbstractQueryImpl {

    private Operator operator;
    private boolean simple = false; //Simple when children are only NodeFilter/EdgeFilter leaves

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

    public int getPropertiesCount() {
        return 0;
    }

    public String getPropertyName(int index) {
        return null;
    }

    public Object getPropertyValue(int index) {
        return null;
    }

    public Filter getFilter() {
        return operator;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    public boolean isSimple() {
        return simple;
    }
}
