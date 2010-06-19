/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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
package org.gephi.data.attributes;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 * @author Martin Škurla
 */
@ServiceProvider(service = AttributeUtils.class)
public class AttributeUtilsImpl extends AttributeUtils {

    @Override
    public boolean isStringColumn(AttributeColumn column) {
        return column.getType().equals(AttributeType.STRING) || column.getType().equals(AttributeType.LIST_STRING);
    }

    @Override
    public boolean isNumberColumn(AttributeColumn column) {
        return column.getType().isNumber();
    }

    private Comparable<?> getMaxValueAccordingToAttributeType(AttributeType attributeType) {
        Comparable<?> max = null;
        switch (attributeType) {
            case BYTE:
                max = Byte.MAX_VALUE;
                break;
            case SHORT:
                max = Short.MAX_VALUE;
                break;
            case INT:
                max = Integer.MAX_VALUE;
                break;
            case LONG:
                max = Long.MAX_VALUE;
                break;
            case FLOAT:
                max = Float.POSITIVE_INFINITY;
                break;
            case DOUBLE:
                max = Double.POSITIVE_INFINITY;
                break;
            default:
                throw new IllegalStateException("Enum type " + attributeType + " is not supported...");
                //what about BigInteger & BigDecimal??? maybe change algorithm and in first iteration
                //the first element will be set to min and every other iteraton the comparison will execute???
                //after that first parameter will no more be needed
        }

        return max;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Comparable getMin(AttributeColumn column, Comparable[] values) {
        if (!isNumberColumn(column)) {
            throw new IllegalArgumentException("Colun must be a number column");
        }

        AttributeType type = column.getType();
        Comparable<?> min = getMaxValueAccordingToAttributeType(type);

        for (Comparable o : values) {
            if (o.compareTo(min) < 0)
                min = o;
        }
        return min;

//        if (type.equals(AttributeType.DOUBLE)) {
//            Double min = Double.POSITIVE_INFINITY;
//            for (Object o : values) {
//                Double ca = (Double) o;
//                if (ca.compareTo(min) < 0) {
//                    min = ca;
//                }
//            }
//            return min;
//        } else if (type.equals(AttributeType.FLOAT)) {
//            Float min = Float.POSITIVE_INFINITY;
//            for (Object o : values) {
//                Float ca = (Float) o;
//                if (ca.compareTo(min) < 0) {
//                    min = ca;
//                }
//            }
//            return min;
//        } else if (type.equals(AttributeType.INT)) {
//            Integer min = Integer.MAX_VALUE;
//            for (Object o : values) {
//                Integer ca = (Integer) o;
//                if (ca.compareTo(min) < 0) {
//                    min = ca;
//                }
//            }
//            return min;
//        } else if (type.equals(AttributeType.LONG)) {
//            Long min = Long.MAX_VALUE;
//            for (Object o : values) {
//                Long ca = (Long) o;
//                if (ca.compareTo(min) < 0) {
//                    min = ca;
//                }
//            }
//            return min;
//        }
//        return null;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Comparable getMax(AttributeColumn column, Comparable[] values) {
        if (!isNumberColumn(column)) {
            throw new IllegalArgumentException("Colun must be a number column");
        }

        AttributeType type = column.getType();
        Comparable<?> max = getMaxValueAccordingToAttributeType(type);

        for (Comparable o : values) {
            if (o.compareTo(max) > 0)
                max = o;
        }
        return max;

//        if (type.equals(AttributeType.DOUBLE)) {
//            Double max = Double.NEGATIVE_INFINITY;
//            for (Object o : values) {
//                Double ca = (Double) o;
//                if (ca.compareTo(max) > 0) {
//                    max = ca;
//                }
//            }
//            return max;
//        } else if (type.equals(AttributeType.FLOAT)) {
//            Float max = Float.NEGATIVE_INFINITY;
//            for (Object o : values) {
//                Float ca = (Float) o;
//                if (ca.compareTo(max) > 0) {
//                    max = ca;
//                }
//            }
//            return max;
//        } else if (type.equals(AttributeType.INT)) {
//            Integer max = Integer.MIN_VALUE;
//            for (Object o : values) {
//                Integer ca = (Integer) o;
//                if (ca.compareTo(max) > 0) {
//                    max = ca;
//                }
//            }
//            return max;
//        } else if (type.equals(AttributeType.LONG)) {
//            Long max = Long.MIN_VALUE;
//            for (Object o : values) {
//                Long ca = (Long) o;
//                if (ca.compareTo(max) > 0) {
//                    max = ca;
//                }
//            }
//            return max;
//        }
//        return null;
    }

    @Override
    public boolean isNodeColumn(AttributeColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }
        AttributeColumnImpl columnImpl = (AttributeColumnImpl) column;
        AttributeTableImpl table = columnImpl.getTable();
        if (table == table.getModel().getNodeTable()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isEdgeColumn(AttributeColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }
        AttributeColumnImpl columnImpl = (AttributeColumnImpl) column;
        AttributeTableImpl table = columnImpl.getTable();
        if (table == table.getModel().getEdgeTable()) {
            return true;
        }
        return false;
    }

    @Override
    public AttributeColumn[] getNumberColumns(AttributeTable table) {
        List<AttributeColumn> res = new ArrayList<AttributeColumn>();
        for (AttributeColumn c : table.getColumns()) {
            if (isNumberColumn(c)) {
                res.add(c);
            }
        }
        return res.toArray(new AttributeColumn[0]);
    }

    @Override
    public AttributeColumn[] getStringColumns(AttributeTable table) {
        List<AttributeColumn> res = new ArrayList<AttributeColumn>();
        for (AttributeColumn c : table.getColumns()) {
            if (isStringColumn(c)) {
                res.add(c);
            }
        }
        return res.toArray(new AttributeColumn[0]);
    }
}
