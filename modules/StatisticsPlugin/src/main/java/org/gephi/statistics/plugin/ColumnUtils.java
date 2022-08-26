package org.gephi.statistics.plugin;

import java.util.Arrays;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;

public class ColumnUtils {

  /**
   * Remove columns from the given table if they match the given ids but don't have the correct type.
   *
   * @param table     table
   * @param columnIds the column ids to remove
   * @param type      the type to check
   */
  public static void cleanUpColumns(Table table, String[] columnIds, Class type) {
    Arrays.stream(columnIds).forEach(id -> {
      Column col = table.getColumn(id);
      if (col != null && !col.getTypeClass().equals(type)) {
        table.removeColumn(id);
      }
    });
  }
}
