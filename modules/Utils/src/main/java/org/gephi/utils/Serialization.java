/*
Copyright 2008-2012 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.utils;

import java.awt.Font;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Class for serialization utils such as writing any object value to a String and retrieving it by String + class name.
 *
 * @author Eduardo Ramos
 */
public class Serialization {

    private final GraphModel graphModel;

    public Serialization() {
        this(null);
    }

    public Serialization(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    private static final Serialization INSTANCE_WITHOUT_GRAPH_MODEL = new Serialization();

    public String toText(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        return toText(value, value.getClass());
    }

    public String toText(Object value, Class valueClass) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }

        if (valueClass.equals(Font.class)) {
            Font f = (Font) value;
            return String.format("%s-%d-%d", f.getName(), f.getStyle(), f.getSize()); //bug 551877
        } else if (isPrimitiveOrPrimitiveWrapper(valueClass) || (Number.class.isAssignableFrom(valueClass) && !Number.class.equals(valueClass))) {
            return String.valueOf(value);
        } else {
            PropertyEditor editor = PropertyEditorManager.findEditor(valueClass);
            if (editor != null) {
                Method setGraphModelMethod = null;
                try {
                    setGraphModelMethod = editor.getClass().getMethod("setGraphModel", GraphModel.class);
                    if (setGraphModelMethod != null) {
                        setGraphModelMethod.invoke(editor, graphModel);
                    }
                } catch (Exception ex) {
                    //NOOP
                }

                try {
                    editor.setValue(value);
                    return editor.getAsText();
                } finally {
                    if (setGraphModelMethod != null) {
                        try {
                            setGraphModelMethod.invoke(editor, (Object[]) null);
                        } catch (Exception ex) {
                            //NOOP
                        }
                    }
                }
            } else {
                return null;
            }
        }
    }

    public Object fromText(String valueStr, Class valueClass) {
        if (String.class.equals(valueClass)) {
            return valueStr;
        } else if (valueClass.equals(Font.class)) {
            try {
                String parts[] = valueStr.split("-");
                return new Font(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));//bug 551877
            } catch (Exception e) {
                return null;
            }
        } else if (isPrimitiveOrPrimitiveWrapper(valueClass)) {
            return parsePrimitiveOrWrapper(valueClass, valueStr);
        } else if (Number.class.isAssignableFrom(valueClass) && !Number.class.equals(valueClass)) {
            return NumberUtils.parseNumber(valueStr, valueClass);
        } else {
            PropertyEditor editor = PropertyEditorManager.findEditor(valueClass);
            if (editor != null) {
                Method setGraphModelMethod = null;
                try {
                    setGraphModelMethod = editor.getClass().getMethod("setGraphModel", GraphModel.class);
                    if (setGraphModelMethod != null) {
                        setGraphModelMethod.invoke(editor, graphModel);
                    }
                } catch (Exception ex) {
                    //NOOP
                }

                try {
                    editor.setAsText(valueStr);
                    return editor.getValue();
                } finally {
                    if (setGraphModelMethod != null) {
                        try {
                            setGraphModelMethod.invoke(editor, (Object[]) null);
                        } catch (Exception ex) {
                            //NOOP
                        }
                    }
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Converts any value to a serialized String. Uses <code>PropertyEditor</code> for serialization except for values of <code>Font</code> class.
     *
     * @param value Value to serialize as String
     * @return Result String or null if the value can't be serialized with a <code>PropertyEditor</code>
     */
    public static String getValueAsText(Object value) {
        return INSTANCE_WITHOUT_GRAPH_MODEL.toText(value);
    }
    
    /**
     * Converts any value to a serialized String. Uses <code>PropertyEditor</code> for serialization except for values of <code>Font</code> class.
     *
     * @param value Value to serialize as String
     * @param valueClass Class to use for the value
     * @return Result String or null if the value can't be serialized with a <code>PropertyEditor</code>
     */
    public static String getValueAsText(Object value, Class valueClass) {
        return INSTANCE_WITHOUT_GRAPH_MODEL.toText(value, valueClass);
    }
        

    /**
     * Deserializes a serialized String of the given class. Uses <code>PropertyEditor</code> for serialization except for values of <code>Font</code> class.
     *
     * @param valueStr String to deserialize
     * @param valueClass Class of the serialized value
     * @return Deserialized value or null if it can't be deserialized with a <code>PropertyEditor</code>
     */
    public static Object readValueFromText(String valueStr, Class valueClass) {
        return INSTANCE_WITHOUT_GRAPH_MODEL.fromText(valueStr, valueClass);
    }

    /**
     * Deserializes a serialized String of the given class name. Returns null if the class can't be found. Uses <code>PropertyEditor</code> for serialization except for values of <code>Font</code>
     * class.
     *
     * @param valueStr String to deserialize
     * @param valueClassStr Class name of the serialized value
     * @return Deserialized value or null if it can't be deserialized with a <code>PropertyEditor</code>
     */
    public static Object readValueFromText(String valueStr, String valueClassStr) {
        try {
            return readValueFromText(valueStr, Class.forName(valueClassStr));
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    public static boolean isPrimitiveOrPrimitiveWrapper(Class<?> type) {
        return (type.isPrimitive() && !void.class.equals(type))
                || Double.class.equals(type) || Float.class.equals(type) || Long.class.equals(type)
                || Integer.class.equals(type) || Short.class.equals(type) || Character.class.equals(type)
                || Byte.class.equals(type) || Boolean.class.equals(type);
    }

    public static Object parsePrimitiveOrWrapper(Class valueClass, String value) {
        if (Boolean.class.equals(valueClass) || Boolean.TYPE == valueClass) {
            return Boolean.parseBoolean(value);
        }
        if (Character.class.equals(valueClass) || Character.TYPE == valueClass) {
            return value.charAt(0);
        }
        if (Byte.class.equals(valueClass) || Byte.TYPE == valueClass) {
            return Byte.parseByte(value);
        }
        if (Short.class.equals(valueClass) || Short.TYPE == valueClass) {
            return Short.parseShort(value);
        }
        if (Integer.class.equals(valueClass) || Integer.TYPE == valueClass) {
            return Integer.parseInt(value);
        }
        if (Long.class.equals(valueClass) || Long.TYPE == valueClass) {
            return Long.parseLong(value);
        }
        if (Float.class.equals(valueClass) || Float.TYPE == valueClass) {
            return Float.parseFloat(value);
        }
        if (Double.class.equals(valueClass) || Double.TYPE == valueClass) {
            return Double.parseDouble(value);
        }

        throw new IllegalArgumentException("Unknown class " + valueClass.getName());
    }
}
