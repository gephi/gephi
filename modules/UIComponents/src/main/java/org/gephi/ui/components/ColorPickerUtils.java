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

package org.gephi.ui.components;

import com.bric.swing.ColorPicker;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ContainerEvent;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Shows the bundled {@link ColorPicker} dialog with its hex field constrained to six hexadecimal
 * digits.
 * <p>
 * The picker reacts to every edit of the hex field by rewriting that same field with the canonical
 * form of the color, from inside the field's own document notification. Swing forbids that, so any
 * paste of a value that is not already canonical - <code>#00FF00</code> for instance - fails on the
 * event dispatch thread instead of applying the color. Constraining the field so it can only ever
 * hold the canonical form leaves the picker with nothing to rewrite, which removes the reentrant
 * write altogether.
 */
public final class ColorPickerUtils {

    private ColorPickerUtils() {
    }

    /**
     * Shows the modal color picker dialog.
     *
     * @param owner          the owner window, may be null
     * @param originalColor  the color the picker initially points to
     * @param includeOpacity whether to add a control for the opacity of the color
     * @return the color the user chose, or null if the dialog was cancelled
     */
    public static Color showDialog(Window owner, Color originalColor, boolean includeOpacity) {
        AWTEventListener listener = event -> {
            if (event instanceof ContainerEvent && event.getID() == ContainerEvent.COMPONENT_ADDED) {
                Component child = ((ContainerEvent) event).getChild();
                if (child instanceof ColorPicker) {
                    constrainHexField((ColorPicker) child);
                }
            }
        };
        //The picker is created inside ColorPicker.showDialog(), so its hex field can only be
        //reached when it gets added to the dialog, which happens before the dialog is displayable.
        Toolkit.getDefaultToolkit().addAWTEventListener(listener, AWTEvent.CONTAINER_EVENT_MASK);
        try {
            return ColorPicker.showDialog(owner, originalColor, includeOpacity);
        } finally {
            Toolkit.getDefaultToolkit().removeAWTEventListener(listener);
        }
    }

    private static void constrainHexField(ColorPicker picker) {
        JTextField hexField = findHexField(picker.getExpertControls());
        if (hexField != null) {
            ((AbstractDocument) hexField.getDocument()).setDocumentFilter(new HexDocumentFilter());
        }
    }

    private static JTextField findHexField(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField && isHexField((JTextField) component)) {
                return (JTextField) component;
            }
            if (component instanceof Container) {
                JTextField hexField = findHexField((Container) component);
                if (hexField != null) {
                    return hexField;
                }
            }
        }
        return null;
    }

    private static boolean isHexField(JTextField field) {
        if (!(field.getDocument() instanceof AbstractDocument)) {
            return false;
        }
        //The hex field is the only one the picker listens to, which also tells the numeric spinner
        //editors apart from it.
        for (DocumentListener listener : ((AbstractDocument) field.getDocument()).getDocumentListeners()) {
            if (listener.getClass().getName().startsWith(ColorPicker.class.getName() + "$")) {
                return true;
            }
        }
        return false;
    }

    static final class HexDocumentFilter extends DocumentFilter {

        private static final int HEX_LENGTH = 6;

        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
            throws BadLocationException {
            overtypeInsert(fb, offset, 0, string, attr);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {
            overtypeInsert(fb, offset, length, text, attrs);
        }

        /**
         * Inserts the hex digits found in {@code text} at {@code offset}, after removing
         * {@code removedLength} characters there (the selection being replaced, if any). Whatever
         * does not fit in the six-digit field afterwards is dropped from the far end of the field
         * rather than from what was just typed or pasted, so editing mid-field overtypes like a
         * fixed-width field instead of silently doing nothing once the field is already full.
         */
        private void overtypeInsert(FilterBypass fb, int offset, int removedLength, String text,
            AttributeSet attrs) throws BadLocationException {
            int docLength = fb.getDocument().getLength();
            int budget = HEX_LENGTH - offset;
            String insertDigits = toHexDigits(text, budget);
            int tailStart = offset + removedLength;
            int tailLength = docLength - tailStart;
            int tailBudget = budget - insertDigits.length();
            int tailCharsToRemove = tailLength - Math.min(tailLength, tailBudget);
            if (tailCharsToRemove > 0) {
                fb.remove(docLength - tailCharsToRemove, tailCharsToRemove);
            }
            super.replace(fb, offset, removedLength, insertDigits, attrs);
        }

        private static String toHexDigits(String text, int maxLength) {
            if (text == null) {
                return "";
            }
            StringBuilder hexDigits = new StringBuilder(Math.max(maxLength, 0));
            for (int i = 0; i < text.length() && hexDigits.length() < maxLength; i++) {
                char c = Character.toUpperCase(text.charAt(i));
                if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F')) {
                    hexDigits.append(c);
                }
            }
            return hexDigits.toString();
        }
    }
}
