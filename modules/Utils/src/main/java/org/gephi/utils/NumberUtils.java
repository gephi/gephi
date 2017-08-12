/*
 Copyright 2008-2017 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2017 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2017 Gephi Consortium.
 */
package org.gephi.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 * @author Eduardo Ramos
 */
public class NumberUtils {

    public static <T extends Number> T parseNumber(String str, Class<T> type) throws UnsupportedOperationException {
        try {
            /*
         * Try to access the staticFactory method for: 
         * Byte, Short, Integer, Long, Double, and Float
             */
            Method m = type.getMethod("valueOf", String.class);
            Object o = m.invoke(type, str);
            return type.cast(o);
        } catch (NoSuchMethodException e1) {
            /* Try to access the constructor for BigDecimal or BigInteger*/
            try {
                Constructor<? extends Number> ctor = type
                        .getConstructor(String.class);
                return (T) ctor.newInstance(str);
            } catch (ReflectiveOperationException e2) {
                /* AtomicInteger and AtomicLong not supported */
                throw new UnsupportedOperationException(
                        "Cannot convert string to " + type.getName());
            }
        } catch (ReflectiveOperationException e2) {
            throw new UnsupportedOperationException("Cannot convert string to "
                    + type.getName());
        }
    }

    public static final double EPS = 1e-5;

    public static boolean equalsEpsilon(double a, double b) {
        return equals(a, b, EPS);
    }

    public static boolean equals(double a, double b, double epsilon) {
        return a == b ? true : Math.abs(a - b) < epsilon;
    }
}
