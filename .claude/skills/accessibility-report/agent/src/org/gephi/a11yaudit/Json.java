package org.gephi.a11yaudit;

import java.util.Collection;
import java.util.Map;

/**
 * Minimal JSON writer for maps, collections, strings, numbers and booleans.
 */
final class Json {

    private Json() {
    }

    static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        write(value, sb, 0);
        return sb.append('\n').toString();
    }

    private static void write(Object value, StringBuilder sb, int depth) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String) {
            string((String) value, sb);
        } else if (value instanceof Number || value instanceof Boolean) {
            if (value instanceof Double && (((Double) value).isNaN() || ((Double) value).isInfinite())) {
                sb.append("null");
            } else {
                sb.append(value);
            }
        } else if (value instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                newline(sb, depth + 1);
                string(String.valueOf(e.getKey()), sb);
                sb.append(':');
                write(e.getValue(), sb, depth + 1);
            }
            if (!first) {
                newline(sb, depth);
            }
            sb.append('}');
        } else if (value instanceof Collection) {
            sb.append('[');
            boolean first = true;
            for (Object o : (Collection<?>) value) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                write(o, sb, depth + 1);
            }
            sb.append(']');
        } else if (value instanceof int[]) {
            sb.append('[');
            int[] a = (int[]) value;
            for (int i = 0; i < a.length; i++) {
                sb.append(i > 0 ? "," : "").append(a[i]);
            }
            sb.append(']');
        } else {
            string(String.valueOf(value), sb);
        }
    }

    // Objects nest a few levels only; arrays of nodes stay on one line each to keep files diffable
    private static void newline(StringBuilder sb, int depth) {
        if (depth <= 3) {
            sb.append('\n').append(" ".repeat(depth));
        }
    }

    private static void string(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
