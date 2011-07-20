/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.datalab.plugin.manipulators.rows.merge;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalab.plugin.manipulators.rows.merge.ui.JoinWithSeparatorUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.rows.merge.AttributeRowsMergeStrategy;
import org.gephi.graph.api.Attributes;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * AttributeRowsMergeStrategy for any String or list column that joins the row values with a separator.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class JoinWithSeparator implements AttributeRowsMergeStrategy {

    public static final String SEPARATOR_SAVED_PREFERENCES = "JoinWithSeparator_Separator";
    private static final String DEFAULT_SEPARATOR = ",";
    private Attributes[] rows;
    private AttributeColumn column;
    private String separator, result;

    public void setup(Attributes[] rows, Attributes selectedRow, AttributeColumn column) {
        this.rows = rows;
        this.column = column;
        separator = NbPreferences.forModule(JoinWithSeparator.class).get(SEPARATOR_SAVED_PREFERENCES, DEFAULT_SEPARATOR);
    }

    public Object getReducedValue() {
        return result;
    }

    public void execute() {
        NbPreferences.forModule(JoinWithSeparator.class).put(SEPARATOR_SAVED_PREFERENCES, separator);
        
        Object value;
        StringBuilder sb;
        final int rowsCount = rows.length;
        final int columnIndex=column.getIndex();
        
        sb = new StringBuilder();
        for (int i = 0; i < rows.length; i++) {
            value = rows[i].getValue(columnIndex);
            if (value != null) {
                sb.append(value.toString());
                if (i < rowsCount - 1) {
                    sb.append(separator);
                }
            }
        }
        result=sb.toString();
    }

    public String getName() {
        return NbBundle.getMessage(AverageNumber.class, "JoinWithSeparator.name");
    }

    public String getDescription() {
        return NbBundle.getMessage(AverageNumber.class, "JoinWithSeparator.description");
    }

    public boolean canExecute() {
        return column.getType().isListType() || column.getType() == AttributeType.STRING;
    }

    public ManipulatorUI getUI() {
        return new JoinWithSeparatorUI();
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 100;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/datalab/plugin/manipulators/resources/join.png", true);
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
