/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Martin Škurla
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
package org.gephi.data.attributes.api;

import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 * @author Martin  Škurla
 */
public abstract class AttributeUtils {

    public abstract boolean isNodeColumn(AttributeColumn column);

    public abstract boolean isEdgeColumn(AttributeColumn column);

    public abstract boolean isColumnOfType(AttributeColumn column, AttributeType type);

    public abstract boolean areAllColumnsOfType(AttributeColumn[] columns, AttributeType type);

    public abstract boolean areAllColumnsOfSameType(AttributeColumn[] columns);

    public abstract boolean isStringColumn(AttributeColumn column);

    public abstract boolean areAllStringColumns(AttributeColumn[] columns);

    public abstract boolean isNumberColumn(AttributeColumn column);

    public abstract boolean areAllNumberColumns(AttributeColumn[] columns);

    public abstract boolean isNumberListColumn(AttributeColumn column);

    public abstract boolean areAllNumberListColumns(AttributeColumn[] columns);

    public abstract boolean isNumberOrNumberListColumn(AttributeColumn column);

    public abstract boolean areAllNumberOrNumberListColumns(AttributeColumn[] columns);

    public abstract boolean isDynamicNumberColumn(AttributeColumn column);

    public abstract boolean areAllDynamicNumberColumns(AttributeColumn[] columns);

    public abstract AttributeColumn[] getNumberColumns(AttributeTable table);

    public abstract AttributeColumn[] getStringColumns(AttributeTable table);

    public abstract AttributeColumn[] getAllCollums(AttributeModel model);

    @SuppressWarnings("rawtypes")
    public abstract Comparable getMin(AttributeColumn column, Comparable[] values);

    @SuppressWarnings("rawtypes")
    public abstract Comparable getMax(AttributeColumn column, Comparable[] values);

    public static synchronized AttributeUtils getDefault() {
        return Lookup.getDefault().lookup(AttributeUtils.class);
    }

    /**
     * Used for export (writes XML date strings).
     *
     * @param d a double to convert from
     *
     * @return an XML date string.
     *
     * @throws IllegalArgumentException if {@code d} is infinite.
     */
    public static String getXMLDateStringFromDouble(double d) {
        try {
            DatatypeFactory dateFactory = DatatypeFactory.newInstance();
            if (d == Double.NEGATIVE_INFINITY) {
                return "-Infinity";
            } else if (d == Double.POSITIVE_INFINITY) {
                return "Infinity";
            }
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTimeInMillis((long) d);
            return dateFactory.newXMLGregorianCalendar(gc).toXMLFormat().substring(0, 23);
        } catch (DatatypeConfigurationException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }
}
