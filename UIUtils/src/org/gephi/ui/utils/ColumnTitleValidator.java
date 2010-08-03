/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.utils;

import org.gephi.data.attributes.api.AttributeTable;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Severity;
import org.netbeans.validation.api.Validator;
import org.openide.util.NbBundle;

/**
 * Utils class to validate a string that contains a title for a column of a <code>AttributeTable</code>.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class ColumnTitleValidator implements Validator<String> {

    private AttributeTable table;

    public ColumnTitleValidator(AttributeTable table) {
        this.table = table;
    }

    public boolean validate(Problems prblms, String string, String t) {
        if (t == null || t.isEmpty()) {
            prblms.add(NbBundle.getMessage(ColumnTitleValidator.class, "ColumnTitleValidator.title.empty"));
            return false;
        } else if (table.hasColumn(t)) {
            prblms.add(NbBundle.getMessage(ColumnTitleValidator.class, "ColumnTitleValidator.title.repeated"));
            return false;
        } else {
            return true;
        }
    }
}
