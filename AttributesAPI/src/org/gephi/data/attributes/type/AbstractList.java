/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/
package org.gephi.data.attributes.type;

import java.util.Arrays;

/**
 * Complex type that defines list of any type of items. Can be created from an array or from single
 * string using either given or default separators. Internal representation of data is array of generic
 * type. This means that every primitive type must be first converted into wrapper type. The exact
 * conversion process from String value into given type is done by {@link TypeConvertor TypeConvertor}
 * class.
 *
 * <br/><br/>
 * <h3>Design guidelines</h3>
 * This is a basic abstract class that every other 'List' class should extend. In order to not misuse
 * the API, every extending type should be one of the following:
 * <ul>
 * <li>helper type which restricts the type parameter and possibly brings some new functionality (e.g.
 *     {@link NumberList NumberList}). This is not final usable type so it should be declared as abstract.
 * <li>final type that extends any of defined helper types or basic class and sets the type parameter
 *     (e.g. there are types for representing all primitive types, String, BigInteger & BigDecimal).
 *     These are final usable types so they should be declared as final.
 * </ul>
 *
 * <h3>Flexibility</h3>
 * The flexibility of this API is done in 2 ways:
 * <ul>
 * <li>We can add functionality by defining conversions from any other type in difference from general
 *     supported types through defining new constructors (e.g. {@link StringList StringList} class can
 *     be created from array of characters). We can also restrict the functionality (e.g. BigInteger &
 *     BigDecimal cannot be created from arrays of primitive types). The conversion process should be
 *     done by {@link TypeConvertor TypeConvertor} type if the conversion can be used in more List
 *     implementations or by 'private static T parseXXX()' method in appropriate List implementation if
 *     only this type uses the conversion (e.g. {@link StringList#parse}).
 * <li>Any other functionality required from 'List' implementations should be done by implementing
 *     appropriate non-static methods in concrete 'List' implementations.
 * </ul>
 *
 * <h3>Extensibility</h3>
 * This API can be simply extended. New 'List' type should extend base or any helper 'List' type. We can
 * create final 'List' implementations as well as helper 'list' implementations with appropriate modifiers
 * (see Design Guidelines). We can define as many constructors responsible for conversions from other
 * types and as many additional methods as we want.<br/>
 * To fully integrate new 'List' type into the whole codebase we have to update following types:
 * <ol>
 * <li>{@link org.gephi.data.attributes.api.AttributeType AttributeType}:
 *     <ol>
 *     <li>add appropriate enum constants
 *     <li>update {@link org.gephi.data.attributes.api.AttributeType#parse(String str) parse(String)} method
 *     </ol>
 * <li>{@link org.gephi.data.attributes.model.DataIndex DataIndex}:
 *     <ol>
 *     <li>add appropriate type represented by Class object into
 *         {@link org.gephi.data.attributes.model.DataIndex#SUPPORTED_TYPES SUPPORTED_TYPES} array
 *     </ol>
 * </ol>
 *
 * This class defines {@link #size method for recognizing size} of the list and
 * {@link #getItem method for getting item by index}.
 * 
 * @param <T> type parameter defining final List type
 *
 * @author Martin Škurla
 *
 * @see TypeConvertor
 */
public abstract class AbstractList<T> {

    public static final String DEFAULT_SEPARATOR = ",|;";
    protected final T[] list;
    private volatile int hashCode = 0;

    public AbstractList(String input, Class<T> finalType) {
        this(input, DEFAULT_SEPARATOR, finalType);
    }

    public AbstractList(String input, String separator, Class<T> finalType) {
        this(TypeConvertor.<T>createArrayFromString(input, separator, finalType));
    }

    public AbstractList(T[] array) {
        this.list = Arrays.copyOf(array, array.length);
    }

    public int size() {
        return list.length;
    }

    public T getItem(int index) {
        if (index >= list.length) {
            return null;
        }

        return list[index];
    }

    public boolean contains(T value) {
        for (int i = 0; i < list.length; i++) {
            if (list[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < list.length; i++) {
            builder.append(list[i]);
            builder.append(',');
        }

        if (list.length > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractList<?>)) {
            return false;
        }

        AbstractList<?> s = (AbstractList<?>) obj;

        if (s.size() != this.size()) {
            return false;
        }

        for (int i = 0; i < list.length; i++) {
            if (this.getItem(i) != s.getItem(i)) {
                if (this.getItem(i)!=null&&!this.getItem(i).equals(s.getItem(i))) {
                    return false;
                }else if(s.getItem(i)!=null&&!s.getItem(i).equals(this.getItem(i))){
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int hash = 7;

            for (int i = 0; i < list.length; i++) {
                hash = 53 * hash + (this.list[i] != null ? this.list[i].hashCode() : 0);
            }
            hashCode = hash;
        }
        return hashCode;
    }
}

