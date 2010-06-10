package org.gephi.neo4j.impl.attributes.same;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;

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
    @SuppressWarnings("rawtypes")
    public Comparable getMin(AttributeColumn column, Comparable[] values) {
        if (!isNumberColumn(column)) {
            throw new IllegalArgumentException("Colun must be a number column");
        }

        AttributeType type = column.getType();
        Comparable<?> min = getMaxValueAccordingToAttributeType(type);

        for (Comparable o : values) {
            if (o.compareTo(min) < 0) {
                min = o;
            }
        }
        return min;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Comparable getMax(AttributeColumn column, Comparable[] values) {
        if (!isNumberColumn(column)) {
            throw new IllegalArgumentException("Colun must be a number column");
        }

        AttributeType type = column.getType();
        Comparable<?> max = getMaxValueAccordingToAttributeType(type);

        for (Comparable o : values) {
            if (o.compareTo(max) > 0) {
                max = o;
            }
        }
        return max;
    }

        @Override
    public boolean isNodeColumn(AttributeColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }
        AttributeColumnImpl columnImpl = (AttributeColumnImpl) column;
//        AttributeTableImpl table = columnImpl.getTable();//TODO >>>>> implement
//        if (table == table.getModel().getNodeTable()) {
//            return true;
//        }
        return false;
    }

    @Override
    public boolean isEdgeColumn(AttributeColumn column) {
        if (column == null) {
            throw new NullPointerException();
        }
        AttributeColumnImpl columnImpl = (AttributeColumnImpl) column;
//        AttributeTableImpl table = columnImpl.getTable();//TODO implement
//        if (table == table.getModel().getEdgeTable()) {
//            return true;
//        }
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
