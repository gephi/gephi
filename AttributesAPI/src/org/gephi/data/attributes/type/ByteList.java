/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>
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
package org.gephi.data.attributes.type;

/**
 * Complex type that define a list of Byte items. Can be created from a byte
 * array, from a Byte array or from single string using either given or default separators.
 * 
 * @author Martin Škurla
 */
public final class ByteList extends NumberList<Byte> {
    
    public ByteList(byte[] primitiveByteArray) {
        super(TypeConvertor.<Byte>convertPrimitiveToWrapperArray(primitiveByteArray));
    }

    public ByteList(Byte[] wrapperByteArray) {
        super(wrapperByteArray);
    }

    public ByteList(String input) {
        this(input, AbstractList.DEFAULT_SEPARATOR);
    }

    public ByteList(String input, String separator) {
        super(input, separator, Byte.class);
    }
}
