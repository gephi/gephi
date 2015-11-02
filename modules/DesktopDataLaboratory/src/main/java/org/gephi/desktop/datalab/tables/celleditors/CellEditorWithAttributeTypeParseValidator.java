/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.desktop.datalab.tables.celleditors;

import java.awt.Color;
import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import org.gephi.graph.api.AttributeUtils;

/**
 *
 * @author Eduardo Ramos<eduramiba@gmail.com>
 */
public class CellEditorWithAttributeTypeParseValidator extends DefaultCellEditor {

    private static final Border RED_BORDER = new LineBorder(Color.red);
    
    private final JTextField textField;
    private final Border originalBorder;
    private final Class<?> typeClass;

    public CellEditorWithAttributeTypeParseValidator(Class<?> typeClass) {
        super(new JTextField());
        this.typeClass = typeClass;
        
        textField = new JTextField();
        originalBorder = textField.getBorder();
    }

    @Override
    public boolean stopCellEditing() {
        String value = getCellEditorValue().toString();
        if(!value.trim().isEmpty()){
            try {
                System.out.println(value);
                AttributeUtils.parse(value, typeClass);
            } catch (Exception e) {
                textField.setBorder(RED_BORDER);
                return false;//Invalid value for type
            }
        }
        
        return super.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        return textField.getText();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column) {

        Object fieldValue = value;
        if (null == fieldValue) {
            fieldValue = "";
        }

        textField.setBorder(originalBorder);
        textField.setEditable(true);
        textField.setText(fieldValue.toString());
        return textField;
    }
}
