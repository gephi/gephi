package org.gephi.desktop.neo4j.ui.util;

import java.util.LinkedList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Škurla
 */
public class GephiUtils {
    private GephiUtils() {}

    public static String[] edgeColumnNames() {
        return columnsToColumnNames(edgeColumns());
    }

    public static String[] nodeColumnNames() {
        return columnsToColumnNames(nodeColumns());
    }

    private static String[] columnsToColumnNames(AttributeColumn[] columns) {
        List<String> columnNames = new LinkedList<String>();

        for (AttributeColumn attributeColumn : columns)
            columnNames.add(attributeColumn.getTitle());

        return columnNames.toArray(new String [0]);
    }

    private static AttributeColumn[] edgeColumns() {
        AttributeTable edgeTable =
                Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();

        return edgeTable.getColumns();
    }

    private static AttributeColumn[] nodeColumns() {
        AttributeTable nodeTable =
                Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();

        return nodeTable.getColumns();
    }
}
