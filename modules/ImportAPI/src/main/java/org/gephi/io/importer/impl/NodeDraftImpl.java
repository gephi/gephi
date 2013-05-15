/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.io.importer.impl;

import org.gephi.io.importer.api.ColumnDraft;
import org.gephi.io.importer.api.NodeDraft;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDraftImpl extends ElementDraftImpl implements NodeDraft {

    //Flag
    protected boolean createdAuto = false;
    //Viz attributes
    protected float x;
    protected float y;
    protected float z;
    protected float size;
    protected boolean fixed;

    public NodeDraftImpl(ImportContainerImpl container, String id) {
        super(container, id);
    }

    //SETTERS
    public void setCreatedAuto(boolean createdAuto) {
        this.createdAuto = createdAuto;
    }

    @Override
    public void setSize(float size) {
        this.size = size;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public void setZ(float z) {
        this.z = z;
    }

    @Override
    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
//
//    public void addAttributeValue(AttributeColumn column, Object value) {
//        if (column.getType().isDynamicType() && !(value instanceof DynamicType)) {
//            if (value instanceof String && !column.getType().equals(AttributeType.DYNAMIC_STRING)) {
//                //Value needs to be parsed
//                value = TypeConvertor.getStaticType(column.getType()).parse((String) value);
//            }
//            //Wrap value in a dynamic type
//            value = DynamicUtilities.createDynamicObject(column.getType(), new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, value));
//        }
//        attributeRow.setValue(column, value);
//    }
//
//    public void addAttributeValue(AttributeColumn column, Object value, String dateFrom, String dateTo) throws IllegalArgumentException {
//        addAttributeValue(column, value, dateFrom, dateTo, false, false);
//    }
//
//    public void addAttributeValue(AttributeColumn column, Object value, String dateFrom, String dateTo, boolean startOpen, boolean endOpen) throws IllegalArgumentException {
//        if (!column.getType().isDynamicType()) {
//            throw new IllegalArgumentException("The column must be dynamic");
//        }
//        Double start = Double.NEGATIVE_INFINITY;
//        Double end = Double.POSITIVE_INFINITY;
//        if (dateFrom != null && !dateFrom.isEmpty()) {
//            try {
//                if (container.getTimeFormat().equals(TimeFormat.DATETIME)) {
//                    start = DynamicUtilities.getDoubleFromXMLDateTimeString(dateFrom);
//                } else if (container.getTimeFormat().equals(TimeFormat.DATE)) {
//                    start = DynamicUtilities.getDoubleFromXMLDateString(dateFrom);
//                } else if (container.getTimeFormat().equals(TimeFormat.TIMESTAMP)) {
//                    start = Double.parseDouble(dateFrom + "000");
//                } else {
//                    start = Double.parseDouble(dateFrom);
//                }
//            } catch (Exception ex) {
//                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateFrom));
//            }
//        }
//        if (dateTo != null && !dateTo.isEmpty()) {
//            try {
//                if (container.getTimeFormat().equals(TimeFormat.DATETIME)) {
//                    end = DynamicUtilities.getDoubleFromXMLDateTimeString(dateTo);
//                } else if (container.getTimeFormat().equals(TimeFormat.DATE)) {
//                    end = DynamicUtilities.getDoubleFromXMLDateString(dateTo);
//                } else if (container.getTimeFormat().equals(TimeFormat.TIMESTAMP)) {
//                    end = Double.parseDouble(dateTo + "000");
//                } else {
//                    end = Double.parseDouble(dateTo);
//                }
//            } catch (Exception ex) {
//                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateTo));
//            }
//        }
//        if ((start == null && end == null) || (start == Double.NEGATIVE_INFINITY && end == Double.POSITIVE_INFINITY)) {
//            throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_Empty"));
//        }
//        if (value instanceof String && !column.getType().equals(AttributeType.DYNAMIC_STRING)) {
//            //Value needs to be parsed
//            AttributeType staticType = TypeConvertor.getStaticType(column.getType());
//            value = staticType.parse((String) value);
//        }
//        Object sourceVal = attributeRow.getValue(column);
//        if (sourceVal != null && sourceVal instanceof DynamicType) {
//            value = DynamicUtilities.createDynamicObject(column.getType(), (DynamicType) sourceVal, new Interval(start, end, startOpen, endOpen, value));
//        } else if (sourceVal != null && !(sourceVal instanceof DynamicType)) {
//            List<Interval> intervals = new ArrayList<Interval>(2);
//            intervals.add(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, sourceVal));
//            intervals.add(new Interval(start, end, startOpen, endOpen, value));
//            value = DynamicUtilities.createDynamicObject(column.getType(), intervals);
//        } else {
//            value = DynamicUtilities.createDynamicObject(column.getType(), new Interval(start, end, startOpen, endOpen, value));
//        }
//        attributeRow.setValue(column, value);
//    }
//
//    public void addTimeInterval(String dateFrom, String dateTo) throws IllegalArgumentException {
//        addTimeInterval(dateFrom, dateTo, false, false);
//    }
//
//    public void addTimeInterval(String dateFrom, String dateTo, boolean startOpen, boolean endOpen) throws IllegalArgumentException {
//        if (timeInterval == null) {
//            timeInterval = new TimeInterval();
//        }
//        Double start = null;
//        Double end = null;
//        if (dateFrom != null && !dateFrom.isEmpty()) {
//            try {
//                if (container.getTimeFormat().equals(TimeFormat.DATETIME)) {
//                    start = DynamicUtilities.getDoubleFromXMLDateTimeString(dateFrom);
//                } else if (container.getTimeFormat().equals(TimeFormat.DATE)) {
//                    start = DynamicUtilities.getDoubleFromXMLDateString(dateFrom);
//                } else if (container.getTimeFormat().equals(TimeFormat.TIMESTAMP)) {
//                    start = Double.parseDouble(dateFrom + "000");
//                } else {
//                    start = Double.parseDouble(dateFrom);
//                }
//            } catch (Exception ex) {
//                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateFrom));
//            }
//        }
//        if (dateTo != null && !dateTo.isEmpty()) {
//            try {
//                if (container.getTimeFormat().equals(TimeFormat.DATETIME)) {
//                    end = DynamicUtilities.getDoubleFromXMLDateTimeString(dateTo);
//                } else if (container.getTimeFormat().equals(TimeFormat.DATE)) {
//                    end = DynamicUtilities.getDoubleFromXMLDateString(dateTo);
//                } else if (container.getTimeFormat().equals(TimeFormat.TIMESTAMP)) {
//                    end = Double.parseDouble(dateTo + "000");
//                } else {
//                    end = Double.parseDouble(dateTo);
//                }
//            } catch (Exception ex) {
//                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateTo));
//            }
//        }
//        if (start == null && end == null) {
//            throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_Empty"));
//        }
//        timeInterval = new TimeInterval(timeInterval, start != null ? start : Double.NEGATIVE_INFINITY, end != null ? end : Double.POSITIVE_INFINITY, startOpen, endOpen);
//    }
//
//    public void setTimeInterval(TimeInterval timeInterval) {
//        this.timeInterval = timeInterval;
//    }

    //GETTERS
    @Override
    public float getSize() {
        return size;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public float getZ() {
        return z;
    }

    @Override
    public boolean isFixed() {
        return fixed;
    }

    public boolean isCreatedAuto() {
        return createdAuto;
    }

    @Override
    public Object getValue(String key) {
        ColumnDraft column = container.getNodeColumn(key);
        if (column != null) {
            return getAttributeValue(((ColumnDraftImpl) column).getIndex());
        }
        return null;
    }

    @Override
    ColumnDraft getColumn(String key, Class type) {
        return container.addNodeColumn(key, type);
    }
}
