
/*
Copyright 2008-2010 Gephi
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
package org.gephi.datalab.spi.values;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.datalab.spi.Manipulator;

/**
 * Manipulator for a single AttributeValue (cells) on right click.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public interface AttributeValueManipulator extends Manipulator{

    /**
     * Prepare the AttributeValue data.
     * AttributeRow and AttributeColumn are provided.
     * @param row Row
     * @param column Column
     */
    void setup (AttributeRow row, AttributeColumn column);
}
