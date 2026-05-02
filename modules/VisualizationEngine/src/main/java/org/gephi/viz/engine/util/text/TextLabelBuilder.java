package org.gephi.viz.engine.util.text;

import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.GraphView;

public class TextLabelBuilder {

    public static String buildText(Element element, GraphView view, Column[] columns) {
        if (columns.length == 0) {
            return null;
        } else if (columns.length == 1) {
            return buildText(element, view, columns[0]);
        } else {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (Column c : columns) {
                String str = buildText(element, view, c);
                if (str == null) {
                    continue;
                }
                if (i++ > 0) {
                    sb.append(" - ");
                }
                sb.append(str);
            }
            String finalStr = sb.toString();
            if (finalStr.isEmpty()) {
                return null;
            } else {
                return finalStr;
            }
        }
    }

    public static String buildText(Element element, GraphView view, Column column) {
        Object val = element.getAttribute(column, view);
        if (val == null) {
            return null;
        }
        if (column.isArray()) {
            return AttributeUtils.printArray(val);
        } else {
            String str = val.toString();
            if (str.isEmpty()) {
                return null;
            } else {
                return str;
            }
        }
    }
}
