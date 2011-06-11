/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.datalab.utils;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;

/**
 * This custom cell editor for <code>TimeInterval</code> is necessary to properly display a <code>TimeInterval</code>
 * with the current <code>TimeFormat</code> representation (as date or double) while it is being edited in table.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class TimeIntervalCellEditor extends DefaultCellEditor {

    private TimeFormat timeFormat = TimeFormat.DOUBLE;

    public TimeIntervalCellEditor(JTextField textField) {
        super(textField);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value != null) {
            value=((TimeInterval) value).toString(timeFormat == TimeFormat.DOUBLE);
        }
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }
}
