/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;

/**
 * A simple tree cell editor helper used to properly display a node while in editing mode.
 * @author Soot Phengsy
 */
class DirectoryCellEditor extends DefaultCellEditor {
    
    private final JPanel editorPanel = new JPanel(new BorderLayout());
    private static JTextField textField;
    private static JTree tree;
    private static JFileChooser fileChooser;
    
    public DirectoryCellEditor(JTree tree, JFileChooser fileChooser, final JTextField textField) {
        super(textField);
        this.tree = tree;
        this.textField = textField;
        this.fileChooser = fileChooser;
    }
    
    public boolean isCellEditable(EventObject event) {
        return ((event instanceof MouseEvent) ? false : true);
    }
    
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
        Component c = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
        DirectoryNode node = (DirectoryNode)value;
        editorPanel.setOpaque(false);
        editorPanel.add(new JLabel(fileChooser.getIcon(node.getFile())), BorderLayout.CENTER);
        editorPanel.add(c, BorderLayout.EAST);
        textField = (JTextField)getComponent();
        String text = fileChooser.getName(node.getFile());
        textField.setText(text);
        textField.setColumns(text.length());
        return editorPanel;
    }
    
    public static JTextField getTextField() {
        return textField;
    }
}
