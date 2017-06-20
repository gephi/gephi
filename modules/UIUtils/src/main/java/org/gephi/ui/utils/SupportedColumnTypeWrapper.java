/*
 Copyright 2008-2013 Gephi
 Authors : Eduardo Ramos<eduramiba@gmail.com>
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
package org.gephi.ui.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.utils.Attributes;

/**
 * Simple wrapper class for column type selection in UI.
 *
 * @author Eduardo Ramos
 */
public class SupportedColumnTypeWrapper implements Comparable<SupportedColumnTypeWrapper> {

    private final Class<?> type;

    public SupportedColumnTypeWrapper(Class type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (AttributeUtils.isArrayType(type)) {
            return String.format("%s list", type.getComponentType().getSimpleName());
        } else {
            return type.getSimpleName();
        }
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SupportedColumnTypeWrapper other = (SupportedColumnTypeWrapper) obj;
        if (this.type != other.type && (this.type == null || !this.type.equals(other.type))) {
            return false;
        }
        return true;
    }

    private static final List<Class> SIMPLE_TYPES_ORDER = Arrays.asList(new Class[]{
        String.class,
        Integer.class,
        Long.class,
        Float.class,
        Double.class,
        Boolean.class,
        BigInteger.class,
        BigDecimal.class,
        Byte.class,
        Short.class,
        Character.class
    });

    /**
     * Order for column types by name. Simple types appear first, then dynamic types and then array/list types.
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(SupportedColumnTypeWrapper other) {
        boolean isArray = type.isArray();
        boolean isArrayOther = other.type.isArray();

        if (isArray != isArrayOther) {
            if (isArray) {
                return 1;
            } else {
                return -1;
            }
        } else {
            boolean isDynamic = AttributeUtils.isDynamicType(type);
            boolean isDynamicOther = AttributeUtils.isDynamicType(other.type);

            if (isDynamic != isDynamicOther) {
                if (isDynamic) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                int i1 = SIMPLE_TYPES_ORDER.indexOf(type);
                int i2 = SIMPLE_TYPES_ORDER.indexOf(other.type);

                if (i1 != -1 && i2 != -1) {
                    return i1 - i2;
                } else {
                    return type.getSimpleName().compareTo(other.type.getSimpleName());
                }
            }
        }
    }

    /**
     * Build a list of column type wrappers from GraphStore supported types.
     *
     * @param graphModel
     * @return Ordered column type wrappers list
     */
    public static List<SupportedColumnTypeWrapper> buildOrderedSupportedTypesList(GraphModel graphModel) {
        TimeRepresentation timeRepresentation = graphModel.getConfiguration().getTimeRepresentation();
        return buildOrderedSupportedTypesList(timeRepresentation);
    }

    /**
     * Build a list of column type wrappers from GraphStore supported types.
     *
     * @param timeRepresentation
     * @return Ordered column type wrappers list
     */
    public static List<SupportedColumnTypeWrapper> buildOrderedSupportedTypesList(TimeRepresentation timeRepresentation) {
        List<SupportedColumnTypeWrapper> supportedTypesWrappers = new ArrayList<>();

        for (Class<?> type : AttributeUtils.getSupportedTypes()) {
            if (type.equals(Map.class) || type.equals(List.class) || type.equals(Set.class)) {
                continue;
                //Not yet supported in Gephi
            }

            if (AttributeUtils.isStandardizedType(type) && Attributes.isTypeAvailable(type, timeRepresentation)) {
                supportedTypesWrappers.add(new SupportedColumnTypeWrapper(type));
            }
        }

        Collections.sort(supportedTypesWrappers);

        return supportedTypesWrappers;
    }
}
