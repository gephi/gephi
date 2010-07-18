/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>
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
package org.gephi.data.attributes.type;

/**
 * Complex type that defines list of items that are numbers.
 *
 * @param <T> type parameter restricted to types extending Number type
 * 
 * @author Martin Škurla
 */
public abstract class NumberList<T extends Number> extends AbstractList<T> {

    public NumberList(T[] wrapperArray) {
        super(wrapperArray);
    }

    public NumberList(String input, Class<T> finalType) {
        this(input, AbstractList.DEFAULT_SEPARATOR, finalType);
    }

    public NumberList(String input, String separator, Class<T> finalType) {
        super(input, separator, finalType);
    }
}

