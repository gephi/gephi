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

/**
 * Class for serialization utils such as writing any object value to a String and retrieving it by String + class name.
 * @author Eduardo Ramos
 */
public class Serialization {

    /**
     * Converts any value to a serialized String. Uses
     * <code>PropertyEditor</code> for serialization except for values of
     * <code>Font</code> class.
     *
     * @param value Value to serialize as String
     * @return Result String or null if the value can't be serialized with a
     * <code>PropertyEditor</code>
     */
    public static String getValueAsText(Object value) {
        if (value.getClass().equals(Font.class)) {
            Font f = (Font) value;
            return String.format("%s-%d-%d", f.getName(), f.getStyle(), f.getSize()); //bug 551877
        } else {
            PropertyEditor editor = PropertyEditorManager.findEditor(value.getClass());
            if (editor != null) {
                editor.setValue(value);
                return editor.getAsText();
            } else {
                return null;
            }
        }
    }

    /**
     * Deserializes a serialized String of the given class. Uses
     * <code>PropertyEditor</code> for serialization except for values of
     * <code>Font</code> class.
     *
     * @param valueStr String to deserialize
     * @param valueClass Class of the serialized value
     * @return Deserialized value or null if it can't be deserialized with a
     * <code>PropertyEditor</code>
     */
    public static Object readValueFromText(String valueStr, Class valueClass) {
        if (valueClass.equals(Font.class)) {
            try {
                String parts[] = valueStr.split("-");
                return new Font(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));//bug 551877
            } catch (Exception e) {
                return null;
            }
        } else {
            PropertyEditor editor = PropertyEditorManager.findEditor(valueClass);
            if (editor != null) {
                editor.setAsText(valueStr);
                return editor.getValue();
            } else {
                return null;
            }
        }
    }

    /**
     * Deserializes a serialized String of the given class name. Returns null if
     * the class can't be found. Uses <code>PropertyEditor</code> for
     * serialization except for values of <code>Font</code> class.
     *
     * @param valueStr String to deserialize
     * @param valueClassStr Class name of the serialized value
     * @return Deserialized value or null if it can't be deserialized with a
     * <code>PropertyEditor</code>
     */
    public static Object readValueFromText(String valueStr, String valueClassStr) {
        try {
            return readValueFromText(valueStr, Class.forName(valueClassStr));
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
}
