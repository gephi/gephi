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
package org.gephi.desktop.datalab.tables.columns;

import org.gephi.graph.api.Column;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Element;

/**
 *
 * @author Eduardo Ramos
 */
public class AttributeDataColumn<T extends Element> implements ElementDataColumn<T> {

    private final AttributeColumnsController attributeColumnsController;
    private final Column column;
    private final Class<?> columnClassForTable;

    public AttributeDataColumn(AttributeColumnsController attributeColumnsController, Column column) {
        this.attributeColumnsController = attributeColumnsController;
        this.column = column;

        this.columnClassForTable = column.getTypeClass();
    }

    @Override
    public Class<?> getColumnClass() {
        return columnClassForTable;
    }

    @Override
    public String getColumnName() {
        return column.getTitle();
    }

    @Override
    public Object getValueFor(T element) {
        return element.getAttribute(column);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.column != null ? this.column.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AttributeDataColumn other = (AttributeDataColumn) obj;
        return this.column == other.column || (this.column != null && this.column.equals(other.column));
    }

    @Override
    public void setValueFor(T element, Object value) {
        attributeColumnsController.setAttributeValue(value, element, column);
    }

    @Override
    public boolean isEditable() {
        return attributeColumnsController.canChangeColumnData(column);
    }
}
