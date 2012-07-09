/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
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

package org.gephi.data.attributes;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.gephi.data.attributes.type.AbstractList;
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

/**
 *
 * @author Martin Škurla
 */
public class ListFactory {
    private ListFactory() {}

    public static AbstractList<?> fromArray(Object array) {
        Class<?> componentType = array.getClass().getComponentType();

        if (componentType == byte.class)
            return new ByteList((byte[]) array);
        else if (componentType == Byte.class)
            return new ByteList((Byte[]) array);

        else if (componentType == short.class)
            return new ShortList((short[]) array);
        else if (componentType == Short.class)
            return new ShortList((Short[]) array);

        else if (componentType == int.class)
            return new IntegerList((int[]) array);
        else if (componentType == Integer.class)
            return new IntegerList((Integer[]) array);

        else if (componentType == long.class)
            return new LongList((long[]) array);
        else if (componentType == Long.class)
            return new LongList((Long[]) array);

        else if (componentType == float.class)
            return new FloatList((float[]) array);
        else if (componentType == Float.class)
            return new FloatList((Float[]) array);

        else if (componentType == double.class)
            return new DoubleList((double[]) array);
        else if (componentType == Double.class)
            return new DoubleList((Double[]) array);

        else if (componentType == boolean.class)
            return new BooleanList((boolean[]) array);
        else if (componentType == Boolean.class)
            return new BooleanList((Boolean[]) array);

        else if (componentType == char.class)
            return new CharacterList((char[]) array);
        else if (componentType == Character.class)
            return new CharacterList((Character[]) array);

        else if (componentType == String.class)
            return new StringList((String[]) array);

        else if (componentType == BigInteger.class)
            return new BigIntegerList((BigInteger[]) array);

        else if (componentType == BigDecimal.class)
            return new BigDecimalList((BigDecimal[]) array);

        throw new AssertionError();
    }
}
