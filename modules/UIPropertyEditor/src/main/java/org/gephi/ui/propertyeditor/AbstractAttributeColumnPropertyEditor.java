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
package org.gephi.ui.propertyeditor;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Lookup;

/**
 *
 * @author Mathieu Bastian
 */
abstract class AbstractAttributeColumnPropertyEditor extends PropertyEditorSupport {

    public enum EditorClass {

        NODE, EDGE, NODEEDGE
    };

    public enum AttributeTypeClass {

        ALL, NUMBER, STRING, DYNAMIC_NUMBER, ALL_NUMBER
    };
    private Column[] columns;
    private Column selectedColumn;
    private final EditorClass editorClass;
    private final AttributeTypeClass attributeTypeClass;

    protected AbstractAttributeColumnPropertyEditor(EditorClass editorClass, AttributeTypeClass attributeClass) {
        this.editorClass = editorClass;
        this.attributeTypeClass = attributeClass;
    }

    protected Column[] getColumns() {
        List<Column> cols = new ArrayList<Column>();
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        if (model != null) {
            if (editorClass.equals(EditorClass.NODE) || editorClass.equals(EditorClass.NODEEDGE)) {
                for (Column column : model.getNodeTable()) {
                    if (attributeTypeClass.equals(AttributeTypeClass.NUMBER) && isNumberColumn(column)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.DYNAMIC_NUMBER) && isDynamicNumberColumn(column)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.ALL_NUMBER) && (isDynamicNumberColumn(column) || isNumberColumn(column))) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.ALL)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.STRING) && isStringColumn(column)) {
                        cols.add(column);
                    }
                }
            }
            if (editorClass.equals(EditorClass.EDGE) || editorClass.equals(EditorClass.NODEEDGE)) {
                for (Column column : model.getEdgeTable()) {
                    if (attributeTypeClass.equals(AttributeTypeClass.NUMBER) && isNumberColumn(column)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.DYNAMIC_NUMBER) && isDynamicNumberColumn(column)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.ALL_NUMBER) && (isDynamicNumberColumn(column) || isNumberColumn(column))) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.ALL)) {
                        cols.add(column);
                    } else if (attributeTypeClass.equals(AttributeTypeClass.STRING) && isStringColumn(column)) {
                        cols.add(column);
                    }
                }
            }
        }
        return cols.toArray(new Column[0]);
    }

    @Override
    public String[] getTags() {
        columns = getColumns();
        //selectedColumn = columns[0];
        String[] tags = new String[columns.length];
        for (int i = 0; i < columns.length; i++) {
            tags[i] = columns[i].getTitle();
        }
        return tags;
    }

    @Override
    public Object getValue() {
        return selectedColumn;
    }

    @Override
    public void setValue(Object value) {
        Column column = (Column) value;
        this.selectedColumn = column;
    }

    @Override
    public String getAsText() {
        if (selectedColumn == null) {
            return "---";
        }
        return selectedColumn.getTitle();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        for (Column c : columns) {
            if (c.getTitle().equals(text)) {
                this.selectedColumn = c;
            }
        }
    }

    public boolean isDynamicNumberColumn(Column column) {
        return AttributeUtils.isDynamicType(column.getTypeClass()) && AttributeUtils.isNumberType(column.getTypeClass());
    }

    public boolean isNumberColumn(Column column) {
        return AttributeUtils.isSimpleType(column.getTypeClass()) && AttributeUtils.isNumberType(column.getTypeClass());
    }

    public boolean isStringColumn(Column column) {
        return column.getTypeClass().equals(String.class);
    }
}
