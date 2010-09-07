/*
Copyright 2008-2010 Gephi
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
package org.gephi.datalab.plugin.manipulators.columns;

import java.util.regex.Pattern;
import org.gephi.datalab.spi.columns.AttributeColumnsManipulator;

/**
 * General abstract class for AttributeColumnManipulators that create a new column from a regular expression and another column.
 * They need a title for the new column and a pattern (regex).
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public abstract class GeneralCreateColumnFromRegex implements AttributeColumnsManipulator{
    protected String title;
    protected Pattern pattern;

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
