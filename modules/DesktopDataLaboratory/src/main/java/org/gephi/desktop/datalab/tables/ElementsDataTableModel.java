/*
 Copyright 2008-2015 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos <eduramiba@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2015 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2015 Gephi Consortium.
 */
package org.gephi.desktop.datalab.tables;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import org.gephi.desktop.datalab.tables.columns.ElementDataColumn;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;

/**
 *
 * @author Eduardo Ramos
 */
public class ElementsDataTableModel<T extends Element> extends AbstractTableModel {
    private T[] elements;
    private ElementDataColumn<T>[] columns;

    public ElementsDataTableModel(T[] elements, ElementDataColumn<T>[] cols) {
        this.elements = elements;
        this.columns = cols;
    }

    @Override
    public int getRowCount() {
        return elements.length;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex].getColumnName();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns[columnIndex].getColumnClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columns[columnIndex].isEditable();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            return columns[columnIndex].getValueFor(elements[rowIndex]);
        } catch (Exception e) {
            /**
             * We need to do this because the JTable might repaint itself 
             * while datalab still has not detected that the column has been deleted 
             * (it does so by polling on graph and table observers).
             * I can't find a better solution...
             */
            return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        columns[columnIndex].setValueFor(elements[rowIndex], aValue);
    }

    public T getElementAtRow(int row) {
        return elements[row];
    }

    public ElementDataColumn<T>[] getColumns() {
        return columns;
    }

    public T[] getElements() {
        return elements;
    }
    
    public void configure(T[] elements, ElementDataColumn<T>[] columns) {
        Set<ElementDataColumn> oldColumns = new HashSet<ElementDataColumn>(Arrays.asList(this.columns));
        Set<ElementDataColumn> newColumns = new HashSet<ElementDataColumn>(Arrays.asList(columns));

        boolean columnsChanged = !oldColumns.equals(newColumns);
        this.columns = columns;
        this.elements = elements;

        if (columnsChanged) {
            fireTableStructureChanged();//Only firing this event if columns change is useful because JXTable will not reset columns width if there is no change
        } else {
            fireTableDataChanged();
        }
    }
    
    /**
     * Column at index or null if it's a fake column.
     * @return 
     */
    public Column getColumnAtIndex(int i){
        if (i >= 0 && i < columns.length) {
            return columns[i].getColumn();
        } else {
            return null;
        }
    }
}
