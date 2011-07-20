/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.spi.rows.merge;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.graph.api.Attributes;

/**
 * <p>Service for defining strategies for merging a column of rows of a table.</p>
 * <p>Has the same interface as a manipulator.</p>
 * <p>When a <code>RowsMergeStrategy</code> is executed it must reduce all values to one that should be returned later when <code>getReducedValue</code> is called</p>
 * @see Manipulator
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeRowsMergeStrategy extends Manipulator{

    /**
     * Prepare column and rows for this merge strategy.
     * At least <b>1</b> row will be set up to merge always.
     * @param rows Rows to merge
     * @param selectedRow Main row fo the row group to merge
     * @param column Column to merge
     */
    void setup(Attributes[] rows, Attributes selectedRow, AttributeColumn column);
    
    /**
     * This method is always called after the strategy is set up and executed.
     * @return Reduced value from all rows and the column
     */
    Object getReducedValue();
}
