/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla <bujacik@gmail.com>
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
package org.gephi.data.attributes.model;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.BigDecimalList;
import org.gephi.data.attributes.type.BigIntegerList;
import org.gephi.data.attributes.type.BooleanList;
import org.gephi.data.attributes.type.ByteList;
import org.gephi.data.attributes.type.CharacterList;
import org.gephi.data.attributes.type.DoubleList;
import org.gephi.data.attributes.type.FloatList;
import org.gephi.data.attributes.type.IntegerList;
import org.gephi.data.attributes.type.LongList;
import org.gephi.data.attributes.type.ShortList;
import org.gephi.data.attributes.type.StringList;
import org.gephi.data.attributes.type.TimeInterval;

/**
 * The index where values of the current {@link IndexedAttributeManager} are. This index stores the objects
 * as {@link WeakReference}, so the {@link AttributeRow} may share the objects reference with this index.
 * Moreover when no more objects possess a reference to a value, the {@link WeakReference} system
 * (i.e. Garbage collector) will automatically clean the old references.
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 * @see AttributeType
 */
public class DataIndex {
    @SuppressWarnings("rawtypes")
    private static final Class[] SUPPORTED_TYPES = {
        String.class,     BigInteger.class,     BigDecimal.class,  TimeInterval.class,
        ByteList.class,   ShortList.class,      IntegerList.class, LongList.class,
        FloatList.class,  DoubleList.class,     BooleanList.class, CharacterList.class,
        StringList.class, BigIntegerList.class, BigDecimalList.class};

    @SuppressWarnings("rawtypes")
    private static Map<Class<?>, WeakHashMap> centralHashMap;

    @SuppressWarnings("rawtypes")
    public DataIndex() {
        centralHashMap = new HashMap<Class<?>, WeakHashMap>();

        for (Class<?> supportedType : SUPPORTED_TYPES)
            putInCentralMap(supportedType);
    }

    private static <T> void putInCentralMap(Class<T> supportedType) {
        centralHashMap.put(supportedType, new WeakHashMap<T, WeakReference<T>>());
    }

    public int countEntries() {
        int entries = 0;

        for (WeakHashMap<?,?> weakHashMap : centralHashMap.values())
            entries += weakHashMap.size();

        return entries;
    }

    @SuppressWarnings("unchecked")
    <T> T pushData(T data) {
        Class<?> classObjectKey = data.getClass();
        WeakHashMap<T, WeakReference<T>> weakHashMap = centralHashMap.get(classObjectKey);

        if (weakHashMap == null)
            return data;

        WeakReference<T> value = weakHashMap.get(data);
        if (value == null) {
            WeakReference<T> weakRef = new WeakReference<T>(data);
            weakHashMap.put(data, weakRef);
            return data;
        }

        return value.get();
    }

    public void clear() {
        for (WeakHashMap<?,?> weakHashMap : centralHashMap.values())
            weakHashMap.clear();
    }
}
